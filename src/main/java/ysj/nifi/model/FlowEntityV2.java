package ysj.nifi.model;

public class FlowEntityV2 {
    private String id;
    private String name;

    public FlowDTOV2 flow;

    public FlowDTOV2 getFlow() {
        return flow;
    }

    public void setFlow(FlowDTOV2 flow) {
        this.flow = flow;
    }

    private FlowEntityV2 breadcrumb;
    private FlowEntityV2 parentBreadcrumb;

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

    public FlowEntityV2 getBreadcrumb() {
        return breadcrumb;
    }

    public void setBreadcrumb(FlowEntityV2 breadcrumb) {
        this.breadcrumb = breadcrumb;
    }

    public FlowEntityV2 getParentBreadcrumb() {
        return parentBreadcrumb;
    }

    public void setParentBreadcrumb(FlowEntityV2 parentBreadcrumb) {
        this.parentBreadcrumb = parentBreadcrumb;
    }
}
