package ysj.nifi.model;


import java.util.List;

public class ControllerHistoryDTO {
    List<ControllerSnapshotEntity>  aggregateSnapshots;
    List<FieldDescriptorDTO>  fieldDescriptors;

    public List<ControllerSnapshotEntity> getAggregateSnapshots() {
        return aggregateSnapshots;
    }

    public void setAggregateSnapshots(List<ControllerSnapshotEntity> aggregateSnapshots) {
        this.aggregateSnapshots = aggregateSnapshots;
    }

    public List<FieldDescriptorDTO> getFieldDescriptors() {
        return fieldDescriptors;
    }

    public void setFieldDescriptors(List<FieldDescriptorDTO> fieldDescriptors) {
        this.fieldDescriptors = fieldDescriptors;
    }
}
