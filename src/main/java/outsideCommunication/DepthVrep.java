package outsideCommunication;

import CommunicationInterface.SensorI;
import coppelia.FloatWA;
import coppelia.IntWA;
import coppelia.IntW;
import coppelia.remoteApi;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DepthVrep implements SensorI {
    private final IntW vision_handles;
    private final remoteApi vrep;
    private final int clientID;
    private int time_graph;
    private List<Float> depth_data;
    private int stage;
    private final int res = 256, print_step = 1;
    private final int max_time_graph = 100;
    private SensorI vision;
    private boolean debug = true; 

    private boolean streamingInitialized = false; 
    private volatile boolean depthStreamingInitialized = false;

    public DepthVrep(remoteApi vrep, int clientid, IntW vision_handles, int stageVision, SensorI vision) {
        this.time_graph = 0;
        depth_data = Collections.synchronizedList(new ArrayList<>(res * res));
        this.vrep = vrep;
        this.stage = stageVision;
        this.vision = vision;
        this.vision_handles = vision_handles;
        this.clientID = clientid;

        for (int i = 0; i < res * res; i++) {
            depth_data.add(0f);
        }
    }

    @Override
    public int getStage() {
        return this.stage;
    }

    @Override
    public void setStage(int newstage) {
        this.stage = newstage;
    }

    private final Object lock = new Object();

    @Override
    public Object getData() {
        synchronized (lock) {
            return getDepthDataInternal();
        }
    }

    private final Object apiLock = new Object();

    private Object getDepthDataInternal() {
        final IntWA resolution = new IntWA(2);
        final FloatWA depthWA  = new FloatWA(0); 
        int rc;

        if (vrep == null || clientID < 0 || vision_handles == null || vision_handles.getValue() <= 0) {
            if (debug) System.err.println("[DepthVrep] client/handle invalid");
            resetDepthData();
            return depth_data;
        }

        synchronized (RemoteApiLock.COPPELIA_LOCK) {
            if (!depthStreamingInitialized) {
                vrep.simxGetVisionSensorDepthBuffer(
                    clientID, vision_handles.getValue(), resolution, depthWA,
                    remoteApi.simx_opmode_streaming
                );
                depthStreamingInitialized = true;
                return depth_data; 
            }

            rc = vrep.simxGetVisionSensorDepthBuffer(
                clientID, vision_handles.getValue(), resolution, depthWA,
                remoteApi.simx_opmode_buffer
            );
        

        if (rc == remoteApi.simx_return_novalue_flag) {
            return depth_data; 
        }
        if (rc != remoteApi.simx_return_ok) {
            if (debug) System.err.println("[DepthVrep] remote error: " + rc );
            depthStreamingInitialized = false;
            resetDepthData();
            return depth_data;
        }

        int[] resArr = resolution.getArray();
        if (resArr == null || resArr.length < 2 || resArr[0] <= 0 || resArr[1] <= 0) {
            if (debug) System.err.println("[DepthVrep] invalid resolution");
            resetDepthData();
            return depth_data;
        }
        int w = resArr[0], h = resArr[1];
        float[] raw = depthWA.getArray();
        if (raw == null || raw.length != w*h) {
            if (debug) System.err.println("[DepthVrep] unexpectable size: " +
                (raw == null ? "null" : raw.length) + " vs " + (w*h));
            resetDepthData();
            return depth_data;
        }

        ensureDepthDataSize(res * res); 
        float[] depth_or = new float[res * res];
        processDepthData(raw, depth_or);
        for (int i = 0; i < depth_or.length; i++) {
            if (i < depth_data.size()) depth_data.set(i, depth_or[i]);
        }
        return depth_data;
        }
    }

    private void ensureDepthDataSize(int size) {
        if (depth_data == null) depth_data = Collections.synchronizedList(new ArrayList<>(size));
        while (depth_data.size() < size) depth_data.add(0f);
    }


    private void processDepthData(float[] temp_dep, float[] depth_or) {
        int count_aux = 0;
        for (int y = 0; y < res; y++) {
            for (int x = 0; x < res; x++) {
                float depth_c = temp_dep[y * res + x];
                depth_or[count_aux] = Math.min(Math.max(depth_c * 10, 0), 10);
                count_aux++;
            }
        }

        switch (stage) {
            case 3:
                for (int i = 0; i < res * res; i++) {
                    depth_data.set(i, depth_or[i]);
                }
                break;
            case 2:
                downscaleData(depth_or, 2);
                break;
            case 1:
                downscaleData(depth_or, 4);
                break;
        }
    }

    private void downscaleData(float[] depth_or, int factor) {
        float meanValue;
        for (int n = 0; n < res / factor; n++) {
            int ni = n * factor;
            int no = ni + factor;
            for (int m = 0; m < res / factor; m++) {
                int mi = m * factor;
                int mo = mi + factor;
                meanValue = 0;

                for (int y = ni; y < no; y++) {
                    for (int x = mi; x < mo; x++) {
                        meanValue += depth_or[y * res + x];
                    }
                }

                float correct_mean = meanValue / (factor * factor);
                for (int y = ni; y < no; y++) {
                    for (int x = mi; x < mo; x++) {
                        depth_data.set(y * res + x, correct_mean);
                    }
                }
            }
        }
    }

    private void resetDepthData() {
        for (int i = 0; i < res * res; i++) {
            depth_data.set(i, 0f);
        }
    }

    @Override
    public void resetData() {}
    @Override
    public void setEpoch(int exp) {}
    @Override
    public int getEpoch() { return 0; }
    @Override
    public int getAux() { return 0; }
    @Override
    public int getMaxActions() { return 0; }
    @Override
    public int getMaxEpochs() { return 0; }
    @Override
    public int getnAct() { return 0; }
    @Override
    public void setnAct(int a) {}
    @Override
    public float getFValues(int i) { return 0; }
    @Override
    public void setFValues(int i, float f) {}
    @Override
    public float getIValues(int i) { return 0; }
    @Override
    public void setIValues(int i, int f) {}
    @Override
    public ArrayList<String> getExecutedAct() { return null; }
    @Override
    public void addAction(String a) {}
    @Override
    public boolean endEpochR() { return false; }
    @Override
    public String getLastAction() { return null; }
    @Override
    public void setLastAction(String a) {}
    @Override
    public String gettype() { return null; }
    @Override
    public void setNextAct(boolean next_ac) {}
    @Override
    public boolean getNextAct() { return false; }
    @Override
    public boolean getNextActR() { return false; }
    @Override
    public void setNextActR(boolean next_ac) {}
    @Override
    public float[] getPosition(String s) { return null; }
    @Override
    public float[] getColor(int i) { return null; }
    @Override
    public boolean endEpoch() { return false; }
    @Override
    public void setCrash(boolean cr) {}
    @Override
    public boolean getCrash() { return false; }
}
