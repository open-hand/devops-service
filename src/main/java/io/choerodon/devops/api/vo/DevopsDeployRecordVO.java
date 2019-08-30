package io.choerodon.devops.api.vo;

import java.util.Date;
import java.util.List;

/**
 * Created by Sheep on 2019/7/29.
 */
public class DevopsDeployRecordVO {

    private Long id;
    private Long projectId;
    private String deployType;
    private Long deployId;
    private String env;
    private String deployStatus;
    private String pipelineName;
    private String pipelineTriggerType;
    private Long deployCreatedBy;
    private String userName;
    private String userImage;
    private Date deployTime;
    private List<PipelineStageRecordVO> stageDTOList;
    private PipelineDetailVO pipelineDetailVO;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getDeployType() {
        return deployType;
    }

    public void setDeployType(String deployType) {
        this.deployType = deployType;
    }

    public Long getDeployId() {
        return deployId;
    }

    public void setDeployId(Long deployId) {
        this.deployId = deployId;
    }

    public String getDeployStatus() {
        return deployStatus;
    }

    public void setDeployStatus(String deployStatus) {
        this.deployStatus = deployStatus;
    }

    public String getPipelineName() {
        return pipelineName;
    }

    public void setPipelineName(String pipelineName) {
        this.pipelineName = pipelineName;
    }

    public String getPipelineTriggerType() {
        return pipelineTriggerType;
    }

    public void setPipelineTriggerType(String pipelineTriggerType) {
        this.pipelineTriggerType = pipelineTriggerType;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }


    public Date getDeployTime() {
        return deployTime;
    }

    public void setDeployTime(Date deployTime) {
        this.deployTime = deployTime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public Long getDeployCreatedBy() {
        return deployCreatedBy;
    }

    public void setDeployCreatedBy(Long deployCreatedBy) {
        this.deployCreatedBy = deployCreatedBy;
    }

    public PipelineDetailVO getPipelineDetailVO() {
        return pipelineDetailVO;
    }

    public void setPipelineDetailVO(PipelineDetailVO pipelineDetailVO) {
        this.pipelineDetailVO = pipelineDetailVO;
    }

    public List<PipelineStageRecordVO> getStageDTOList() {
        return stageDTOList;
    }

    public void setStageDTOList(List<PipelineStageRecordVO> stageDTOList) {
        this.stageDTOList = stageDTOList;
    }
}
