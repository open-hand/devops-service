package io.choerodon.devops.api.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import java.util.List;

import io.choerodon.devops.infra.annotation.WillDeleted;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/7 22:18
 */
@WillDeleted
public class CiCdPipelineRecordVO {
    private Long id;
    @ApiModelProperty("gitlab流水线记录id")
    private Long gitlabPipelineId;
    @ApiModelProperty("流水线id")
    private Long pipelineId;
    @ApiModelProperty("流水线状态")
    private String status;
    @ApiModelProperty("触发用户")
    private String userName;
    @ApiModelProperty("触发分支")
    private String gitlabTriggerRef;
    @ApiModelProperty("创建时间")
    private Date createdDate;
    @ApiModelProperty("结束时间")
    private Date finishedDate;
    @ApiModelProperty("执行耗时")
    private Long durationSeconds;
    private List<CiCdStageRecordVO> stageRecordVOList;
    private CiCdPipelineVO ciCdPipelineVO;

    private IamUserDTO userDTO;
    @ApiModelProperty("提交信息")
    private CustomCommitVO commit;

    @JsonIgnore
    @ApiModelProperty("最后更新时间")
    private Date lastUpdateDate;

    private String pipelineName;
    private String triggerType;
    private Long projectId;
    private String processInstanceId;
    private String stageName;
    private Long jobRecordId;
    private Long stageRecordId;
    private String type;
    private Boolean execute;
    private String errorInfo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGitlabPipelineId() {
        return gitlabPipelineId;
    }

    public void setGitlabPipelineId(Long gitlabPipelineId) {
        this.gitlabPipelineId = gitlabPipelineId;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getGitlabTriggerRef() {
        return gitlabTriggerRef;
    }

    public void setGitlabTriggerRef(String gitlabTriggerRef) {
        this.gitlabTriggerRef = gitlabTriggerRef;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
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

    public List<CiCdStageRecordVO> getStageRecordVOList() {
        return stageRecordVOList;
    }

    public void setStageRecordVOList(List<CiCdStageRecordVO> stageRecordVOList) {
        this.stageRecordVOList = stageRecordVOList;
    }

    public CiCdPipelineVO getCiCdPipelineVO() {
        return ciCdPipelineVO;
    }

    public void setCiCdPipelineVO(CiCdPipelineVO ciCdPipelineVO) {
        this.ciCdPipelineVO = ciCdPipelineVO;
    }

    public IamUserDTO getUserDTO() {
        return userDTO;
    }

    public void setUserDTO(IamUserDTO userDTO) {
        this.userDTO = userDTO;
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

    public String getPipelineName() {
        return pipelineName;
    }

    public void setPipelineName(String pipelineName) {
        this.pipelineName = pipelineName;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public Long getJobRecordId() {
        return jobRecordId;
    }

    public void setJobRecordId(Long jobRecordId) {
        this.jobRecordId = jobRecordId;
    }

    public Long getStageRecordId() {
        return stageRecordId;
    }

    public void setStageRecordId(Long stageRecordId) {
        this.stageRecordId = stageRecordId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getExecute() {
        return execute;
    }

    public void setExecute(Boolean execute) {
        this.execute = execute;
    }

    public String getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo;
    }
}
