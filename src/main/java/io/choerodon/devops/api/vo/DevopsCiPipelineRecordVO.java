package io.choerodon.devops.api.vo;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.infra.dto.iam.IamUserDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/7 22:18
 */
public class DevopsCiPipelineRecordVO extends DevopsPipelineRecordVO {


    @ApiModelProperty("gitlab流水线记录id")
    private Long gitlabPipelineId;

    @ApiModelProperty("流水线id")
    private Long ciPipelineId;
    @ApiModelProperty("触发用户")
    private String username;
    @ApiModelProperty("触发分支")
    private String gitlabTriggerRef;
    @ApiModelProperty("结束时间")
    private Date finishedDate;
    @ApiModelProperty("执行耗时")
    private Long durationSeconds;
    @ApiModelProperty("界面展示id")
    private String viewId;


    private Boolean unrelatedFlag = false;
    @ApiModelProperty("gitlab 记录地址")
    private String gitlabPipelineUrl;
    @ApiModelProperty("ci阶段记录的集合")
    private List<DevopsCiStageRecordVO> stageRecordVOS;
    @ApiModelProperty("关联流水线信息")
    private CiCdPipelineVO ciCdPipelineVO;
    @ApiModelProperty("创建者信息")
    private IamUserDTO iamUserDTO;
    @ApiModelProperty("提交信息")
    private CustomCommitVO commit;

    @JsonIgnore
    @ApiModelProperty("最后更新时间")
    private Date lastUpdateDate;
    @ApiModelProperty("关联gitlab project id")
    private Long gitlabProjectId;
    @ApiModelProperty("gitlab commit sha")
    private String commitSha;

    @ApiModelProperty("gitlab source")
    private String source;
    @ApiModelProperty("待审核状态时需要的一些数据")
    private List<DevopsCiPipelineAuditVO> pipelineAuditInfo;

    private IamUserDTO trigger;

    public IamUserDTO getTrigger() {
        return trigger;
    }

    public void setTrigger(IamUserDTO trigger) {
        this.trigger = trigger;
    }

    public Boolean getUnrelatedFlag() {
        return unrelatedFlag;
    }

    public void setUnrelatedFlag(Boolean unrelatedFlag) {
        this.unrelatedFlag = unrelatedFlag;
    }

    public String getGitlabPipelineUrl() {
        return gitlabPipelineUrl;
    }

    public void setGitlabPipelineUrl(String gitlabPipelineUrl) {
        this.gitlabPipelineUrl = gitlabPipelineUrl;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<DevopsCiPipelineAuditVO> getPipelineAuditInfo() {
        return pipelineAuditInfo;
    }

    public void setPipelineAuditInfo(List<DevopsCiPipelineAuditVO> pipelineAuditInfo) {
        this.pipelineAuditInfo = pipelineAuditInfo;
    }

    public String getViewId() {
        return viewId;
    }

    public void setViewId(String viewId) {
        this.viewId = viewId;
    }

    public String getCommitSha() {
        return commitSha;
    }

    public void setCommitSha(String commitSha) {
        this.commitSha = commitSha;
    }

    public Long getGitlabProjectId() {
        return gitlabProjectId;
    }

    public void setGitlabProjectId(Long gitlabProjectId) {
        this.gitlabProjectId = gitlabProjectId;
    }


    public Long getGitlabPipelineId() {
        return gitlabPipelineId;
    }

    public void setGitlabPipelineId(Long gitlabPipelineId) {
        this.gitlabPipelineId = gitlabPipelineId;
    }

    public Long getCiPipelineId() {
        return ciPipelineId;
    }

    public void setCiPipelineId(Long ciPipelineId) {
        this.ciPipelineId = ciPipelineId;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public List<DevopsCiStageRecordVO> getStageRecordVOS() {
        return stageRecordVOS;
    }

    public void setStageRecordVOS(List<DevopsCiStageRecordVO> stageRecordVOS) {
        this.stageRecordVOS = stageRecordVOS;
    }

    public IamUserDTO getIamUserDTO() {
        return iamUserDTO;
    }

    public void setIamUserDTO(IamUserDTO iamUserDTO) {
        this.iamUserDTO = iamUserDTO;
    }

    public CiCdPipelineVO getCiCdPipelineVO() {
        return ciCdPipelineVO;
    }

    public void setCiCdPipelineVO(CiCdPipelineVO ciCdPipelineVO) {
        this.ciCdPipelineVO = ciCdPipelineVO;
    }

    public String getGitlabTriggerRef() {
        return gitlabTriggerRef;
    }

    public void setGitlabTriggerRef(String gitlabTriggerRef) {
        this.gitlabTriggerRef = gitlabTriggerRef;
    }

    public CustomCommitVO getCommit() {
        return commit;
    }

    public void setCommit(CustomCommitVO commit) {
        this.commit = commit;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }
}
