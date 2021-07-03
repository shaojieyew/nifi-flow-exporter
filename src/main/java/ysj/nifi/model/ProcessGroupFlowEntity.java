package ysj.nifi.model;


import com.davis.client.model.*;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ProcessGroupFlowEntity {
    private FlowEntityV2 processGroupFlow;

    public FlowEntityV2 getProcessGroupFlow() {
        return processGroupFlow;
    }

    public void setProcessGroupFlow(FlowEntityV2 processGroupFlow) {
        this.processGroupFlow = processGroupFlow;
    }

}
