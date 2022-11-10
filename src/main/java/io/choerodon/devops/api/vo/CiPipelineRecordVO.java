package io.choerodon.devops.api.vo;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.infra.dto.iam.IamUserDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/7 22:18
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CiPipelineRecordVO extends BaseDomain {

    @Encrypt
    @ApiModelProperty("ci 执行记录的id")
    private Long id;
    @Encrypt
    @ApiModelProperty("ci 执行记录的id")
    private Long ciRecordId;
    @ApiModelProperty("gitlab的流水线id")
    private Long gitlabPipelineId;
    @ApiModelProperty("cicd 流水线的状态")
    private String status;
    @ApiModelProperty("流水线触发分支：master、release")
    private String gitlabTriggerRef;
    @ApiModelProperty("流水线名称")
    private String pipelineName;
    @ApiModelProperty("关联gitlab project id")
    private Long gitlabProjectId;
    @ApiModelProperty("界面展示id")
    private String viewId;
    @ApiModelProperty("关联应用服务名称")
    private String appServiceName;
    @ApiModelProperty("执行耗时")
    private Long durationSeconds;
    @Encrypt
    @ApiModelProperty("关联流水线id")
    private Long ciPipelineId;
    @Encrypt
    @ApiModelProperty("关联流水线id")
    private Long pipelineId;
    @ApiModelProperty("创建者id")
    private Long createdBy;

    @ApiModelProperty("创建者信息")
    private IamUserDTO iamUserDTO;
    @ApiModelProperty("流水线提交信息")
    private CustomCommitVO commit;
    @ApiModelProperty("关联流水线信息")
    private CiCdPipelineVO ciCdPipelineVO;
    @ApiModelProperty("ci和cd阶段记录的集合")
    private List<DevopsCiStageRecordVO> stageRecordVOS;
    @ApiModelProperty("待审核状态时需要的一些数据")
    private List<DevopsCiPipelineAuditVO> pipelineAuditInfo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCiPipelineId() {
        return ciPipelineId;
    }

    public void setCiPipelineId(Long ciPipelineId) {
        this.ciPipelineId = ciPipelineId;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public IamUserDTO getIamUserDTO() {
        return iamUserDTO;
    }

    public void setIamUserDTO(IamUserDTO iamUserDTO) {
        this.iamUserDTO = iamUserDTO;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }


    public String getAppServiceName() {
        return appServiceName;
    }

    public void setAppServiceName(String appServiceName) {
        this.appServiceName = appServiceName;
    }

    public String getViewId() {
        return viewId;
    }

    public void setViewId(String viewId) {
        this.viewId = viewId;
    }

    public Long getGitlabProjectId() {
        return gitlabProjectId;
    }

    public void setGitlabProjectId(Long gitlabProjectId) {
        this.gitlabProjectId = gitlabProjectId;
    }

    public String getPipelineName() {
        return pipelineName;
    }

    public void setPipelineName(String pipelineName) {
        this.pipelineName = pipelineName;
    }

    public CiCdPipelineVO getCiCdPipelineVO() {
        return ciCdPipelineVO;
    }

    public void setCiCdPipelineVO(CiCdPipelineVO ciCdPipelineVO) {
        this.ciCdPipelineVO = ciCdPipelineVO;
    }

    public List<DevopsCiPipelineAuditVO> getPipelineAuditInfo() {
        return pipelineAuditInfo;
    }

    public void setPipelineAuditInfo(List<DevopsCiPipelineAuditVO> pipelineAuditInfo) {
        this.pipelineAuditInfo = pipelineAuditInfo;
    }

    public Long getCiRecordId() {
        return ciRecordId;
    }

    public void setCiRecordId(Long ciRecordId) {
        this.ciRecordId = ciRecordId;
    }

    public Long getGitlabPipelineId() {
        return gitlabPipelineId;
    }

    public void setGitlabPipelineId(Long gitlabPipelineId) {
        this.gitlabPipelineId = gitlabPipelineId;
    }

    public List<DevopsCiStageRecordVO> getStageRecordVOS() {
        return stageRecordVOS;
    }

    public void setStageRecordVOS(List<DevopsCiStageRecordVO> stageRecordVOS) {
        this.stageRecordVOS = stageRecordVOS;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    @Override
    public String toString() {
        return "CiCdPipelineRecordVO{" +
                ", ciRecordId=" + ciRecordId +
                ", gitlabPipelineId=" + gitlabPipelineId +
                ", status='" + status + '\'' +
                ", gitlabTriggerRef='" + gitlabTriggerRef + '\'' +
                ", commit=" + commit +
                ", ciCdPipelineVO=" + ciCdPipelineVO +
                ", stageRecordVOS=" + stageRecordVOS +
                ", pipelineName='" + pipelineName + '\'' +
                ", gitlabProjectId=" + gitlabProjectId +
                ", viewId='" + viewId + '\'' +
                '}';
    }
}
