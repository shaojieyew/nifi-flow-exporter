package ysj.nifi.model;

public class ControllerHistoryEntity {
    ControllerHistoryDTO statusHistory;

    public ControllerHistoryDTO getStatusHistory() {
        return statusHistory;
    }

    public void setStatusHistory(ControllerHistoryDTO statusHistory) {
        this.statusHistory = statusHistory;
    }
}
