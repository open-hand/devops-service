package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.choerodon.mybatis.entity.BaseDTO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:41 2019/4/3
 * Description:
 */
@Table(name = "devops_pipeline_user_record_rel")
public class PipelineUserRecordRelationshipDTO extends BaseDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Long pipelineRecordId;
    private Long stageRecordId;
    private Long taskRecordId;

    public PipelineUserRecordRelationshipDTO() {
    }

    public PipelineUserRecordRelationshipDTO(Long pipelineRecordId, Long stageRecordId, Long taskRecordId) {
        this.pipelineRecordId = pipelineRecordId;
        this.stageRecordId = stageRecordId;
        this.taskRecordId = taskRecordId;
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

    public Long getPipelineRecordId() {
        return pipelineRecordId;
    }

    public void setPipelineRecordId(Long pipelineRecordId) {
        this.pipelineRecordId = pipelineRecordId;
    }

    public Long getStageRecordId() {
        return stageRecordId;
    }

    public void setStageRecordId(Long stageRecordId) {
        this.stageRecordId = stageRecordId;
    }

    public Long getTaskRecordId() {
        return taskRecordId;
    }

    public void setTaskRecordId(Long taskRecordId) {
        this.taskRecordId = taskRecordId;
    }
}
