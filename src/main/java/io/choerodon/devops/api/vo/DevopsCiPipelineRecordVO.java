package io.choerodon.devops.api.vo;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class DevopsCiPipelineRecordVO {
    @Encrypt
    private Long id;

    @ApiModelProperty("gitlab流水线记录id")
    private Long gitlabPipelineId;

    @Encrypt
    @ApiModelProperty("流水线id")
    private Long ciPipelineId;

    @ApiModelProperty("流水线状态")
    private String status;
    @ApiModelProperty("触发用户")
    private String username;
    @ApiModelProperty("触发分支")
    private String gitlabTriggerRef;
    @ApiModelProperty("创建时间")
    private Date createdDate;
    @ApiModelProperty("结束时间")
    private Date finishedDate;
    @ApiModelProperty("执行耗时")
    private Long durationSeconds;
    private List<DevopsCiStageRecordVO> stageRecordVOList;
    private CiCdPipelineVO devopsCiPipelineVO;

    private IamUserDTO userDTO;
    @ApiModelProperty("提交信息")
    private CustomCommitVO commit;

    @JsonIgnore
    @ApiModelProperty("最后更新时间")
    private Date lastUpdateDate;

    private Long gitlabProjectId;

    public Long getGitlabProjectId() {
        return gitlabProjectId;
    }

    public void setGitlabProjectId(Long gitlabProjectId) {
        this.gitlabProjectId = gitlabProjectId;
    }

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

    public Long getCiPipelineId() {
        return ciPipelineId;
    }

    public void setCiPipelineId(Long ciPipelineId) {
        this.ciPipelineId = ciPipelineId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public List<DevopsCiStageRecordVO> getStageRecordVOList() {
        return stageRecordVOList;
    }

    public void setStageRecordVOList(List<DevopsCiStageRecordVO> stageRecordVOList) {
        this.stageRecordVOList = stageRecordVOList;
    }

    public IamUserDTO getUserDTO() {
        return userDTO;
    }

    public void setUserDTO(IamUserDTO userDTO) {
        this.userDTO = userDTO;
    }

    public CiCdPipelineVO getDevopsCiPipelineVO() {
        return devopsCiPipelineVO;
    }

    public void setDevopsCiPipelineVO(CiCdPipelineVO devopsCiPipelineVO) {
        this.devopsCiPipelineVO = devopsCiPipelineVO;
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
