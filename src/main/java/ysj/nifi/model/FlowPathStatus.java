package ysj.nifi.model;

public class FlowPathStatus {

        boolean isRunning;
        long congestionRate;
        long latency;

        public FlowPathStatus(boolean isRunning, long congestionRate, long latency) {
            this.isRunning = isRunning;
            this.congestionRate = congestionRate;
            this.latency = latency;
        }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public long getCongestionRate() {
        return congestionRate;
    }

    public void setCongestionRate(long congestionRate) {
        this.congestionRate = congestionRate;
    }

    public long getLatency() {
        return latency;
    }

    public void setLatency(long latency) {
        this.latency = latency;
    }

    @Override
    public String toString() {
        return "FlowPathStatus{" +
                "isRunning=" + isRunning +
                ", congestionRate=" + congestionRate +
                ", latency=" + latency +
                '}';
    }
}
