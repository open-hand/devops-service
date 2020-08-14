package io.choerodon.devops.infra.dto;

import java.util.Date;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/4/2 17:25
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_ci_job_record")
public class DevopsCiJobRecordDTO extends AuditDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ApiModelProperty("gitlab job记录id")
    private Long gitlabJobId;
    @ApiModelProperty("流水线记录id")
    private Long ciPipelineRecordId;
    @ApiModelProperty("gitlabProjectId")
    private Long gitlabProjectId;
    @ApiModelProperty("阶段名称")
    private String stage;
    @ApiModelProperty("触发用户")
    private Long triggerUserId;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("任务类型")
    private String type;
    @ApiModelProperty("job状态")
    private String status;
    @ApiModelProperty("job开始时间")
    private Date startedDate;
    @ApiModelProperty("job结束时间")
    private Date finishedDate;
    @ApiModelProperty("job执行时间")
    private Long durationSeconds;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGitlabJobId() {
        return gitlabJobId;
    }

    public void setGitlabJobId(Long gitlabJobId) {
        this.gitlabJobId = gitlabJobId;
    }

    public Long getCiPipelineRecordId() {
        return ciPipelineRecordId;
    }

    public void setCiPipelineRecordId(Long ciPipelineRecordId) {
        this.ciPipelineRecordId = ciPipelineRecordId;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public Long getTriggerUserId() {
        return triggerUserId;
    }

    public void setTriggerUserId(Long triggerUserId) {
        this.triggerUserId = triggerUserId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getStartedDate() {
        return startedDate;
    }

    public void setStartedDate(Date startedDate) {
        this.startedDate = startedDate;
    }

    public Date getFinishedDate() {
        return finishedDate;
    }

    public void setFinishedDate(Date finishedDate) {
        this.finishedDate = finishedDate;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getGitlabProjectId() {
        return gitlabProjectId;
    }

    public void setGitlabProjectId(Long gitlabProjectId) {
        this.gitlabProjectId = gitlabProjectId;
    }
}
