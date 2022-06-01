package io.choerodon.devops.api.vo;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.vo.iam.UserVO;

public class DevopsCdStageRecordVO extends StageRecordVO {
    @Encrypt
    private Long id;
    @ApiModelProperty("阶段中的任务记录")
    private List<DevopsCdJobRecordVO> jobRecordVOList;
    @Encrypt
    @ApiModelProperty("所属流水线id")
    private Long pipelineId;
    @ApiModelProperty("是否并行执行")
    private Boolean parallel;
    @Encrypt
    @ApiModelProperty("所属阶段id")
    private Long stageId;
    @ApiModelProperty("审核人员信息")
    private List<PipelineUserVO> userDTOS;
    @ApiModelProperty("阶段顺序")
    private Long sequence;
    @ApiModelProperty("阶段已审核人员的信息 阶段间只有或签")
    private UserVO iamUserDTO;

    @Override
    public Long getSequence() {
        return sequence;
    }

    @Override
    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public UserVO getIamUserDTO() {
        return iamUserDTO;
    }

    public void setIamUserDTO(UserVO iamUserDTO) {
        this.iamUserDTO = iamUserDTO;
    }

    public List<DevopsCdJobRecordVO> getJobRecordVOList() {
        return jobRecordVOList;
    }

    public void setJobRecordVOList(List<DevopsCdJobRecordVO> jobRecordVOList) {
        this.jobRecordVOList = jobRecordVOList;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public Boolean getParallel() {
        return parallel;
    }

    public void setParallel(Boolean parallel) {
        this.parallel = parallel;
    }

    public Long getStageId() {
        return stageId;
    }

    public void setStageId(Long stageId) {
        this.stageId = stageId;
    }

    public List<PipelineUserVO> getUserDTOS() {
        return userDTOS;
    }

    public void setUserDTOS(List<PipelineUserVO> userDTOS) {
        this.userDTOS = userDTOS;
    }


}
