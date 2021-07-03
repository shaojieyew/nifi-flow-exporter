package ysj.nifi.model;

import java.util.Objects;

public class FlowPath {
    private NifiComponent startComponent;
    private NifiComponent endComponent;
    private FlowPathStatus flowPathStatus;

    public FlowPath(NifiComponent startComponent, NifiComponent endComponent, FlowPathStatus flowPathStatus) {
        this.startComponent = startComponent;
        this.endComponent = endComponent;
        this.flowPathStatus = flowPathStatus;
    }

    public NifiComponent getStartComponent() {
        return startComponent;
    }

    public void setStartComponent(NifiComponent startComponent) {
        this.startComponent = startComponent;
    }

    public NifiComponent getEndComponent() {
        return endComponent;
    }

    public void setEndComponent(NifiComponent endComponent) {
        this.endComponent = endComponent;
    }

    public FlowPathStatus getFlowPathStatus() {
        return flowPathStatus;
    }

    public void setFlowPathStatus(FlowPathStatus flowPathStatus) {
        this.flowPathStatus = flowPathStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FlowPath)) return false;
        FlowPath flowPath = (FlowPath) o;
        return Objects.equals(getStartComponent(), flowPath.getStartComponent()) &&
                Objects.equals(getEndComponent(), flowPath.getEndComponent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStartComponent(), getEndComponent());
    }
}
