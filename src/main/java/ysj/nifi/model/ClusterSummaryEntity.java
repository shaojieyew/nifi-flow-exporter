package ysj.nifi.model;

public class ClusterSummaryEntity {

    public ClusterSummaryDTO clusterSummary;

    public ClusterSummaryDTO getClusterSummary() {
        return clusterSummary;
    }

    public void setClusterSummary(ClusterSummaryDTO clusterSummary) {
        this.clusterSummary = clusterSummary;
    }

    public class ClusterSummaryDTO{
        int connectedNodeCount;
        int totalNodeCount;
        boolean connectedToCluster;
        boolean clustered;

        public int getConnectedNodeCount() {
            return connectedNodeCount;
        }

        public void setConnectedNodeCount(int connectedNodeCount) {
            this.connectedNodeCount = connectedNodeCount;
        }

        public int getTotalNodeCount() {
            return totalNodeCount;
        }

        public void setTotalNodeCount(int totalNodeCount) {
            this.totalNodeCount = totalNodeCount;
        }

        public boolean isConnectedToCluster() {
            return connectedToCluster;
        }

        public void setConnectedToCluster(boolean connectedToCluster) {
            this.connectedToCluster = connectedToCluster;
        }

        public boolean isClustered() {
            return clustered;
        }

        public void setClustered(boolean clustered) {
            this.clustered = clustered;
        }


    }


}
