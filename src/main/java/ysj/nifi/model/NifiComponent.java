package ysj.nifi.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class NifiComponent {
    String id;
    String name;
    String flowPath;
    String type;
    String status;
    String groupId;
    String group;
    Object snapshot;
    Set<NifiComponent> nextComponent = new HashSet<>();

    public Object getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(Object snapshot) {
        this.snapshot = snapshot;
    }

    public Set<NifiComponent> getNextComponent() {
        return nextComponent;
    }

    public void setNextComponent(Set<NifiComponent> nextComponent) {
        this.nextComponent = nextComponent;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFlowPath() {
        return flowPath;
    }

    public void setFlowPath(String flowPath) {
        this.flowPath = flowPath;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NifiComponent)) return false;
        NifiComponent that = (NifiComponent) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getFlowPath(), that.getFlowPath()) &&
                Objects.equals(getType(), that.getType()) &&
                Objects.equals(getGroupId(), that.getGroupId()) &&
                Objects.equals(getGroup(), that.getGroup());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getFlowPath(), getType(), getGroupId(), getGroup());
    }

    @Override
    public String toString() {
        return "NifiComponent{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", flowPath='" + flowPath + '\'' +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                ", groupId='" + groupId + '\'' +
                ", group='" + group + '\'' +
                ", snapshot=" + snapshot +
                ", nextComponent=" + nextComponent +
                '}';
    }
}
