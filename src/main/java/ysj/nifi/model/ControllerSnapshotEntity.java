package ysj.nifi.model;

import org.joda.time.DateTime;

import java.util.Map;

public class ControllerSnapshotEntity {
    DateTime timestamp;
    Map<String, Long> statusMetrics;

    public DateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Long> getStatusMetrics() {
        return statusMetrics;
    }

    public void setStatusMetrics(Map<String, Long> statusMetrics) {
        this.statusMetrics = statusMetrics;
    }
}
