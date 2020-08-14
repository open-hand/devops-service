package io.choerodon.devops.infra.dto.workflow;

import java.util.List;


/**
 * Created by Sheep on 2019/4/2.
 */
public class DevopsPipelineDTO {

    private Long pipelineRecordId;
    private String pipelineName;
    private String businessKey;
    private List<String> userNames;
    private Boolean multiAssign;

    private List<DevopsPipelineStageDTO> stages;

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public Long getPipelineRecordId() {
        return pipelineRecordId;
    }

    public void setPipelineRecordId(Long pipelineRecordId) {
        this.pipelineRecordId = pipelineRecordId;
    }

    public List<DevopsPipelineStageDTO> getStages() {
        return stages;
    }

    public void setStages(List<DevopsPipelineStageDTO> stages) {
        this.stages = stages;
    }

    public String getPipelineName() {
        return pipelineName;
    }

    public void setPipelineName(String pipelineName) {
        this.pipelineName = pipelineName;
    }

    public List<String> getUserNames() {
        return userNames;
    }

    public void setUserNames(List<String> userNames) {
        this.userNames = userNames;
    }

    public Boolean getMultiAssign() {
        return multiAssign;
    }

    public void setMultiAssign(Boolean multiAssign) {
        this.multiAssign = multiAssign;
    }
}
