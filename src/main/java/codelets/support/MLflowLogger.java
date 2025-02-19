/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codelets.support;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpResponse;

public class MLflowLogger {
    private static final String MLFLOW_TRACKING_URI = "http://localhost:5000";
    private static final String EXPERIMENT_ID = "435097086020200986";

    public static void logMetric(String runId, String key, Number value, int step) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(MLFLOW_TRACKING_URI + "/api/2.0/mlflow/runs/log-metric");
            post.setHeader("Content-Type", "application/json");

            Map<String, Object> payload = new HashMap<>();
            payload.put("run_id", runId);
            payload.put("key", key);
            payload.put("value", value);
            payload.put("timestamp", System.currentTimeMillis());
            payload.put("step", step); // Include the step
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(payload);

            post.setEntity(new StringEntity(json));
            client.execute(post);
            //System.out.println("Logged metric: " + key + " = " + value+ " (step: " + step + ")");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String startRun(String runName) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(MLFLOW_TRACKING_URI + "/api/2.0/mlflow/runs/create");
            post.setHeader("Content-Type", "application/json");

            Map<String, Object> payload = new HashMap<>();
            payload.put("experiment_id", EXPERIMENT_ID);

            // Add run name as a tag
            Map<String, String> tags = new HashMap<>();
            tags.put("mlflow.runName", runName);
            payload.put("tags", tags);

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(payload);

            post.setEntity(new StringEntity(json));

            HttpResponse response = client.execute(post);
            String responseBody = new String(response.getEntity().getContent().readAllBytes());
            System.out.println("Start Run Response: " + responseBody);

            // Parse the response to extract the run ID
            Map<String, Object> responseMap = mapper.readValue(responseBody, Map.class);
            if (response.getStatusLine().getStatusCode() == 200) {
                Map<String, Object> run = (Map<String, Object>) responseMap.get("run");
                Map<String, Object> info = (Map<String, Object>) run.get("info");
                return info.get("run_id").toString();
            } else {
                System.err.println("Failed to start run: " + responseBody);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void endRun(String runId) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(MLFLOW_TRACKING_URI + "/api/2.0/mlflow/runs/update");
            post.setHeader("Content-Type", "application/json");

            // Prepare the payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("run_id", runId);
            payload.put("status", "FINISHED"); // Mark the run as finished

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(payload);

            post.setEntity(new StringEntity(json));

            // Execute the request
            HttpResponse response = client.execute(post);
            String responseBody = new String(response.getEntity().getContent().readAllBytes());
            System.out.println("End Run Response: " + responseBody);

            if (response.getStatusLine().getStatusCode() != 200) {
                System.err.println("Failed to end run: " + responseBody);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
