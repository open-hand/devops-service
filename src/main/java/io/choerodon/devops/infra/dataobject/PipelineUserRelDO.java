package io.choerodon.devops.infra.dataobject;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:43 2019/4/3
 * Description:
 */
@VersionAudit
@ModifyAudit
@Table(name = "devops_pipeline_user_rel")
public class PipelineUserRelDO extends AuditDomain {
    @Id
    @GeneratedValue
    private Long id;
    private Long userId;
    private Long pipelineId;
    private Long stageId;
    private Long taskId;

    public PipelineUserRelDO(Long pipelineId, Long stageId, Long taskId) {
        this.pipelineId = pipelineId;
        this.stageId = stageId;
        this.taskId = taskId;
    }

    public PipelineUserRelDO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public Long getStageId() {
        return stageId;
    }

    public void setStageId(Long stageId) {
        this.stageId = stageId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
}
