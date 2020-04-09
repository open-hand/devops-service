package io.choerodon.devops.api.vo;

import java.util.Date;
import java.util.List;

import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.swagger.annotations.ApiModelProperty;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/7 22:18
 */
public class DevopsCiPipelineRecordVO {
    private Long id;
    @ApiModelProperty("gitlab流水线记录id")
    private Long gitlabPipelineId;
    @ApiModelProperty("流水线id")
    private Long ciPipelineId;
    @ApiModelProperty("流水线状态")
    private String status;
    @ApiModelProperty("触发用户")
    private String username;
    @ApiModelProperty("创建时间")
    private Date createdDate;
    @ApiModelProperty("结束时间")
    private Date finishedDate;
    @ApiModelProperty("执行耗时")
    private Long durationSeconds;
    private List<DevopsCiStageRecordVO>  stageRecordVOList;
    private DevopsCiPipelineVO devopsCiPipelineVO;

    private IamUserDTO userDTO;

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

    public DevopsCiPipelineVO getDevopsCiPipelineVO() {
        return devopsCiPipelineVO;
    }

    public void setDevopsCiPipelineVO(DevopsCiPipelineVO devopsCiPipelineVO) {
        this.devopsCiPipelineVO = devopsCiPipelineVO;
    }
}
