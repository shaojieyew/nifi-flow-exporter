package ysj.nifi.model;


import com.davis.client.model.ConnectionStatusSnapshotDTO;
import com.google.gson.annotations.SerializedName;

public class ConnectionStatusSnapshotDTOV2 extends ConnectionStatusSnapshotDTO {
    @SerializedName("percentUseCount")
    private long percentUseCount = 0;
    @SerializedName("percentUseBytes")
    private long percentUseBytes = 0;

    public long getPercentUseCount() {
        return percentUseCount;
    }

    public void setPercentUseCount(long percentUseCount) {
        this.percentUseCount = percentUseCount;
    }

    public long getPercentUseBytes() {
        return percentUseBytes;
    }

    public void setPercentUseBytes(long percentUseBytes) {
        this.percentUseBytes = percentUseBytes;
    }
}
