package ysj.nifi.model;

import com.davis.client.model.ConnectionEntity;
import com.davis.client.model.ConnectionStatusDTO;
import com.davis.client.model.ConnectionStatusSnapshotDTO;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ConnectionEntityV2 extends ConnectionEntity {
    @SerializedName("status")
    private ConnectionStatusDTOV2 status;

    @Override
    public ConnectionStatusDTOV2 getStatus() {
        return status;
    }

    public void setStatus(ConnectionStatusDTOV2 status) {
        this.status = status;
    }
}
