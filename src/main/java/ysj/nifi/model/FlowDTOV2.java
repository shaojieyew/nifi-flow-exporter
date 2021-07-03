package ysj.nifi.model;

import com.davis.client.model.ConnectionEntity;
import com.davis.client.model.ConnectionStatusDTO;
import com.davis.client.model.ConnectionStatusSnapshotDTO;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class FlowDTOV2  {
    @SerializedName("connections")
    private List<ConnectionEntityV2> connections = new ArrayList();

    public List<ConnectionEntityV2> getConnections() {
        return this.connections;
    }

    public void setConnections(List<ConnectionEntityV2> connections) {
        this.connections = connections;
    }
}
