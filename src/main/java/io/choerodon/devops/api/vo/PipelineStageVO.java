package io.choerodon.devops.api.vo;

import java.util.Date;
import java.util.List;

import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.infra.annotation.WillDeleted;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:20 2019/4/8
 * Description:
 */
@WillDeleted
public class PipelineStageVO {
    @Encrypt
    private Long id;
    private String stageName;
    private String triggerType;
    @Encrypt
    private List<Long> stageUserRels;
    private Integer isParallel;
    private List<PipelineTaskVO> pipelineTaskVOs;
    @Encrypt
    private Long pipelineId;
    private Long projectId;
    private Long objectVersionNumber;
    private Date lastUpdateDate;

    public List<Long> getStageUserRels() {
        return stageUserRels;
    }

    public void setStageUserRels(List<Long> stageUserRels) {
        this.stageUserRels = stageUserRels;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public Integer getIsParallel() {
        return isParallel;
    }

    public void setIsParallel(Integer isParallel) {
        this.isParallel = isParallel;
    }

    public List<PipelineTaskVO> getPipelineTaskVOs() {
        return pipelineTaskVOs;
    }

    public void setPipelineTaskVOs(List<PipelineTaskVO> pipelineTaskVOs) {
        this.pipelineTaskVOs = pipelineTaskVOs;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
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
}
