package ysj.nifi.model;


import com.davis.client.model.ConnectionStatusDTO;
import com.google.gson.annotations.SerializedName;

public class ConnectionStatusDTOV2 extends ConnectionStatusDTO {

    @SerializedName("aggregateSnapshot")
    private ConnectionStatusSnapshotDTOV2 aggregateSnapshot = null;

    @Override
    public ConnectionStatusSnapshotDTOV2 getAggregateSnapshot() {
        return aggregateSnapshot;
    }

    public void setAggregateSnapshot(ConnectionStatusSnapshotDTOV2 aggregateSnapshot) {
        this.aggregateSnapshot = aggregateSnapshot;
    }
}