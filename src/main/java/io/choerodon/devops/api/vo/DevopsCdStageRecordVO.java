package io.choerodon.devops.api.vo;

import java.util.List;

import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.vo.iam.UserVO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;

public class DevopsCdStageRecordVO extends StageRecordVO {
    @Encrypt
    private Long id;
    private List<DevopsCdJobRecordVO> jobRecordVOList;
    private String triggerType;
    @Encrypt
    private Long pipelineId;
    private Boolean parallel;
    @Encrypt
    private Long stageId;
    private List<PipelineUserVO> userDTOS;
    private Boolean index;


    private Long executionTime;
    private Long sequence;

    //阶段已审核人员的信息 阶段间只有或签
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

    public void setExecutionTime(Long executionTime) {
        this.executionTime = executionTime;
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


    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
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

    public Long getExecutionTime() {
        return executionTime;
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

    public Boolean getIndex() {
        return index;
    }

    public void setIndex(Boolean index) {
        this.index = index;
    }


}
