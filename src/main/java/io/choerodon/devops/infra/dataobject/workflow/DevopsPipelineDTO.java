package io.choerodon.devops.infra.dataobject.workflow;

import java.util.List;


/**
 * Created by Sheep on 2019/4/2.
 */
public class DevopsPipelineDTO {

    private Long bussinessId;
    private List<DevopsPipelineStageDTO> stages;
    private Long projectId;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getBussinessId() {
        return bussinessId;
    }

    public void setBussinessId(Long bussinessId) {
        this.bussinessId = bussinessId;
    }

    public List<DevopsPipelineStageDTO> getStages() {
        return stages;
    }

    public void setStages(List<DevopsPipelineStageDTO> stages) {
        this.stages = stages;
    }
}
