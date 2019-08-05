package io.choerodon.devops.api.vo;

import java.util.Date;
import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:23 2019/4/8
 * Description:
 */
public class PipelineTaskVO {
    private Long id;
    private String name;
    private String type;
    private List<Long> taskUserRelDTOS;
    private Integer isCountersigned;
    private Long appDeployId;
    private PipelineAppServiceDeployVO pipelineAppServiceDeployVO;
    private Long stageId;
    private Long projectId;
    private Long objectVersionNumber;
    private Date lastUpdateDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Long> getTaskUserRelDTOS() {
        return taskUserRelDTOS;
    }

    public void setTaskUserRelDTOS(List<Long> taskUserRelDTOS) {
        this.taskUserRelDTOS = taskUserRelDTOS;
    }

    public Integer getIsCountersigned() {
        return isCountersigned;
    }

    public void setIsCountersigned(Integer isCountersigned) {
        this.isCountersigned = isCountersigned;
    }

    public Long getAppDeployId() {
        return appDeployId;
    }

    public void setAppDeployId(Long appDeployId) {
        this.appDeployId = appDeployId;
    }

    public Long getStageId() {
        return stageId;
    }

    public void setStageId(Long stageId) {
        this.stageId = stageId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public PipelineAppServiceDeployVO getPipelineAppServiceDeployVO() {
        return pipelineAppServiceDeployVO;
    }

    public void setPipelineAppServiceDeployVO(PipelineAppServiceDeployVO pipelineAppServiceDeployVO) {
        this.pipelineAppServiceDeployVO = pipelineAppServiceDeployVO;
    }
}
