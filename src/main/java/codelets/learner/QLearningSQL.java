package codelets.learner;

import br.unicamp.cst.learning.QLearning;
import java.sql.*;
import java.util.*;

public class QLearningSQL extends QLearning {
    private LinkedHashMap<Integer, HashMap<String, Double>> Q = new LinkedHashMap<>() {
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > 100000; // Limit cache size to prevent memory overload
        }
    };
    private String fileName;
    private ArrayList<String> actionsList;
    private double alpha = 0.5; // Learning rate
    private double gamma = 0.9; // Discount factor
    private final double UPDATE_THRESHOLD = 1e-6; // Minimal change threshold for updates
    private boolean showDebugMessages = false;
    private long seed;
    private Random rad;
    public QLearningSQL(String fileName, ArrayList<String> allActionsList, long seed) {
        super();
        this.fileName = fileName;
        this.actionsList = allActionsList;
        this.seed = seed;
        createTable();
        enableWALMode();
        initializeQTable();
        
        rad = new Random();
        rad.setSeed(this.seed);
        
    }

    public void setFilename(String fileName) {
        this.fileName = fileName;
    }

    public void initializeQTable() {
    System.out.println("Initializing Q-table from " + this.fileName);
    String querySQL = "SELECT state, action, value FROM qtable";

    try (Connection conn = this.connect();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(querySQL)) {

        while (rs.next()) {
            int state = rs.getInt("state");
            String action = rs.getString("action");
            double value = rs.getDouble("value");

            // Load values into in-memory Q-table
            Q.computeIfAbsent(state, k -> new HashMap<>()).put(action, value);
        }

        System.out.println("Q-table initialization complete.");

    } catch (SQLException e) {
        System.out.println("Error during Q-table initialization: " + e.getMessage());
    }
}

    
    @Override
    public void recoverQ() {
        System.out.println("Recovering Q-table from " + this.fileName);
        String querySQL = "SELECT state, action, value FROM qtable";

        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(querySQL)) {

            while (rs.next()) {
                int state = rs.getInt("state");
                String action = rs.getString("action");
                double value = rs.getDouble("value");

                Q.computeIfAbsent(state, k -> new HashMap<>()).put(action, value);
            }

        } catch (SQLException e) {
            System.out.println("Error recovering Q-table: " + e.getMessage());
        }
    }

    private Connection connect() throws SQLException {
        String url = "jdbc:sqlite:" + this.fileName;
        return DriverManager.getConnection(url);
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS qtable ("
                   + "state INTEGER NOT NULL, action TEXT NOT NULL, value REAL NOT NULL,"
                   + "PRIMARY KEY (state, action));";
        try (Connection conn = this.connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("Error creating table: " + e.getMessage());
        }
    }

    public void setQ(double Qval, int state, String action) {
        double currentQVal = getQ(state, action);

        if (Math.abs(currentQVal - Qval) >= UPDATE_THRESHOLD) {
            Q.computeIfAbsent(state, k -> new HashMap<>()).put(action, Qval);

            String updateSQL = "INSERT OR REPLACE INTO qtable (state, action, value) VALUES (?, ?, ?)";
            try (Connection conn = this.connect();
                 PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
                pstmt.setInt(1, state);
                pstmt.setString(2, action);
                pstmt.setDouble(3, Qval);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Error updating Q-table in database: " + e.getMessage());
            }
        }
    }

        public double getQ(int state, String action) {
        // First, check if the value exists in the in-memory cache
        if (Q.containsKey(state) && Q.get(state).containsKey(action)) {
            return Q.get(state).get(action);
        }

        double qValue = 0.0;

        // Check if the state-action pair exists in the SQL database
        String querySQL = "SELECT value FROM qtable WHERE state = ? AND action = ?";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(querySQL)) {
            pstmt.setInt(1, state);
            pstmt.setString(2, action);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // If the state-action pair exists, fetch its value
                    qValue = rs.getDouble("value");
                    // Add to in-memory cache
                    Q.computeIfAbsent(state, k -> new HashMap<>()).put(action, qValue);
                    return qValue;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving Q-value: " + e.getMessage());
        }

        // If the state-action pair doesn't exist in the database, initialize it
        initializeStateActionPair(state, action, qValue);

        return qValue;
    }

    private void initializeStateActionPair(int state, String action, double defaultQValue) {
        // Insert default Q-value into the database and in-memory cache
        String insertSQL = "INSERT INTO qtable (state, action, value) VALUES (?, ?, ?)";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setInt(1, state);
            pstmt.setString(2, action);
            pstmt.setDouble(3, defaultQValue);
            pstmt.executeUpdate();

            // Add to in-memory cache
            Q.computeIfAbsent(state, k -> new HashMap<>()).put(action, defaultQValue);
        } catch (SQLException e) {
            System.out.println("Error initializing state-action pair: " + e.getMessage());
        }
    }


    public double update(int stateIWas, String actionIDid, double rewardIGot) {
        double deltaQ = 0.0;

        if (stateIWas != -1 && actionIDid != null) {
            double Qas = this.getQ(stateIWas, actionIDid);
            double MaxQ = this.maxQ(stateIWas);
            double newQ = Qas + alpha * (rewardIGot + gamma * MaxQ - Qas);

            deltaQ = Math.abs(newQ - Qas);
            this.setQ(newQ, stateIWas, actionIDid);
        }

        return deltaQ;
    }

    public double maxQ(int state) {
        HashMap<String, Double> actionsQ = Q.get(state);
        if (actionsQ == null) return 0.0;

        return actionsQ.values().stream().max(Double::compare).orElse(0.0);
    }

    public String getAction(int state) {
        String selectedAction = null;

        if (actionsList.isEmpty()) {
            System.out.println("Error: actionsList is empty.");
            return null;
        }

        
        double rd = rad.nextDouble();
        if (rd <= this.getE()) {
            selectedAction = actionsList.get(rad.nextInt(actionsList.size()));
        } else {
            HashMap<String, Double> actionsQ = Q.get(state);
            if (actionsQ == null) {
                selectedAction = actionsList.get(rad.nextInt(actionsList.size()));
            } else {
                selectedAction = actionsQ.entrySet()
                        .stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse(null);
            }
        }

        return selectedAction;
    }

    /*public void initializeQTableBulk(int totalStates, ArrayList<String> allActionsList) {
        String insertSQL = "INSERT OR REPLACE INTO qtable (state, action, value) VALUES (?, ?, 0)";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            conn.setAutoCommit(false);

            for (int state = 0; state < totalStates; state++) {
                for (String action : allActionsList) {
                    pstmt.setInt(1, state);
                    pstmt.setString(2, action);
                    pstmt.addBatch();
                }

                if (state % 100000 == 0) { // Commit in chunks of 1000 states
                    if (state % 10000000==0) {
                        float pstate = state/totalStates*100;
                    
                        System.out.println(state+"/"+totalStates+" - "+pstate+"%");
                    }
                    pstmt.executeBatch();
                    conn.commit();
                }
            }

            pstmt.executeBatch(); // Final commit
            conn.commit();

        } catch (SQLException e) {
            System.out.println("Error during bulk initialization: " + e.getMessage());
        }
    }
*/
    public int getTotalRowCount() {
        String countSQL = "SELECT COUNT(*) AS total FROM qtable";
        int totalRows = 0;

        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countSQL)) {
            if (rs.next()) {
                totalRows = rs.getInt("total");
            }
        } catch (SQLException e) {
            System.out.println("Error counting rows in Q-table: " + e.getMessage());
        }

        return totalRows;
    }

    private void enableWALMode() {
        try (Connection conn = this.connect(); Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA journal_mode=WAL;");
        } catch (SQLException e) {
            System.out.println("Failed to enable WAL mode: " + e.getMessage());
        }
    }
}
