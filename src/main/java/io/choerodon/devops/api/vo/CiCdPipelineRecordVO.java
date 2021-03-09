package io.choerodon.devops.api.vo;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.vo.iam.UserVO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/7 22:18
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CiCdPipelineRecordVO extends BaseDomain {

    private Long devopsPipelineRecordRelId;
    @Encrypt
    /**
     * ci 执行记录的id
     */
    private Long ciRecordId;
    @Encrypt
    /**
     * cd 执行记录的id
     */
    private Long cdRecordId;
    /**
     * gitlab的流水线id
     */
    private Long gitlabPipelineId;

    /**
     * cd流水线记录的状态
     */
    private String cdStatus;
    /**
     * ci的流水线记录状态
     */
    private String ciStatus;
    /**
     * cicd 流水线的状态
     */
    private String status;
    private String gitlabTriggerRef;
    private CustomCommitVO commit;

    private CiCdPipelineVO ciCdPipelineVO;

    /**
     * ci和cd阶段记录的集合
     */
    private List<StageRecordVO> stageRecordVOS;
    /**
     * 待审核状态是需要一些数据
     */
    private DevopsCdPipelineDeatilVO devopsCdPipelineDeatilVO;
    private String pipelineName;
    private Long gitlabProjectId;
    private String viewId;

    private String appServiceName;

    private Long createdBy;

    private IamUserDTO iamUserDTO;

    private Long durationSeconds;
    @Encrypt
    private Long pipelineId;

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

    public DevopsCdPipelineDeatilVO getDevopsCdPipelineDeatilVO() {
        return devopsCdPipelineDeatilVO;
    }

    public void setDevopsCdPipelineDeatilVO(DevopsCdPipelineDeatilVO devopsCdPipelineDeatilVO) {
        this.devopsCdPipelineDeatilVO = devopsCdPipelineDeatilVO;
    }

    public Long getDevopsPipelineRecordRelId() {
        return devopsPipelineRecordRelId;
    }

    public void setDevopsPipelineRecordRelId(Long devopsPipelineRecordRelId) {
        this.devopsPipelineRecordRelId = devopsPipelineRecordRelId;
    }

    public Long getCiRecordId() {
        return ciRecordId;
    }

    public void setCiRecordId(Long ciRecordId) {
        this.ciRecordId = ciRecordId;
    }

    public Long getCdRecordId() {
        return cdRecordId;
    }

    public void setCdRecordId(Long cdRecordId) {
        this.cdRecordId = cdRecordId;
    }


    public Long getGitlabPipelineId() {
        return gitlabPipelineId;
    }

    public void setGitlabPipelineId(Long gitlabPipelineId) {
        this.gitlabPipelineId = gitlabPipelineId;
    }

    public String getCdStatus() {
        return cdStatus;
    }

    public void setCdStatus(String cdStatus) {
        this.cdStatus = cdStatus;
    }

    public String getCiStatus() {
        return ciStatus;
    }

    public void setCiStatus(String ciStatus) {
        this.ciStatus = ciStatus;
    }

    public List<StageRecordVO> getStageRecordVOS() {
        return stageRecordVOS;
    }

    public void setStageRecordVOS(List<StageRecordVO> stageRecordVOS) {
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
                "devopsPipelineRecordRelId=" + devopsPipelineRecordRelId +
                ", ciRecordId=" + ciRecordId +
                ", cdRecordId=" + cdRecordId +
                ", gitlabPipelineId=" + gitlabPipelineId +
                ", cdStatus='" + cdStatus + '\'' +
                ", ciStatus='" + ciStatus + '\'' +
                ", status='" + status + '\'' +
                ", gitlabTriggerRef='" + gitlabTriggerRef + '\'' +
                ", commit=" + commit +
                ", ciCdPipelineVO=" + ciCdPipelineVO +
                ", stageRecordVOS=" + stageRecordVOS +
                ", devopsCdPipelineDeatilVO=" + devopsCdPipelineDeatilVO +
                ", pipelineName='" + pipelineName + '\'' +
                ", gitlabProjectId=" + gitlabProjectId +
                ", viewId='" + viewId + '\'' +
                '}';
    }
}
