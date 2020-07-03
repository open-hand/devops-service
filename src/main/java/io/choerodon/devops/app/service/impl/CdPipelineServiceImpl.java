package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.kubernetes.Command;
import io.choerodon.devops.app.service.CdPipelineService;
import io.choerodon.devops.app.service.DevopsCdAuditRecordService;
import io.choerodon.devops.app.service.DevopsCdJobRecordService;
import io.choerodon.devops.app.service.DevopsCdStageRecordService;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.workflow.DevopsPipelineDTO;
import io.choerodon.devops.infra.dto.workflow.DevopsPipelineStageDTO;
import io.choerodon.devops.infra.dto.workflow.DevopsPipelineTaskDTO;
import io.choerodon.devops.infra.enums.DeployType;
import io.choerodon.devops.infra.enums.JobTypeEnum;
import io.choerodon.devops.infra.util.GenerateUUID;
import io.choerodon.devops.infra.util.TypeUtil;

/**
 * @author scp
 * @date 2020/7/3
 * @description
 */
@Service
public class CdPipelineServiceImpl implements CdPipelineService {
    @Autowired
    private DevopsCdStageRecordService stageRecordService;

    @Autowired
    private DevopsCdAuditRecordService devopsCdAuditRecordService;

    @Autowired
    private DevopsCdJobRecordService devopsCdJobRecordService;

    @Override
    public void cdHostImageDeploy(Long pipelineRecordId, Long cdStageRecordId, Long cdJobRecordId) {

    }

    /**
     * 准备workflow创建实例所需数据
     * 为此workflow下所有stage创建记录
     */
    public DevopsPipelineDTO createCDWorkFlowDTO(Long pipelineRecordId) {
        // 1.
        DevopsPipelineDTO devopsPipelineDTO = new DevopsPipelineDTO();
        devopsPipelineDTO.setPipelineRecordId(pipelineRecordId);
        devopsPipelineDTO.setBusinessKey(GenerateUUID.generateUUID());

        // 2.
        List<DevopsPipelineStageDTO> devopsPipelineStageDTOS = new ArrayList<>();
        List<DevopsCdStageRecordDTO> stageRecordDTOList = stageRecordService.queryByPipelineRecordId(pipelineRecordId);
        if (CollectionUtils.isEmpty(stageRecordDTOList)) {
            return null;
        }

        for (int i = 0; i < stageRecordDTOList.size(); i++) {
            // 3.
            DevopsPipelineStageDTO stageDTO = new DevopsPipelineStageDTO();
            DevopsCdStageRecordDTO stageRecordDTO = stageRecordDTOList.get(i);
            stageDTO.setStageRecordId(stageRecordDTO.getId());
            if (stageRecordDTO.getTriggerType().equals(DeployType.MANUAL.getType())) {
                List<DevopsCdAuditRecordDTO> stageAuditRecordDTOS = devopsCdAuditRecordService.queryByStageRecordId(stageRecordDTO.getId());
                if (CollectionUtils.isEmpty(stageAuditRecordDTOS)) {
                    throw new CommonException("error.audit.stage.noUser");
                }
                List<String> users = stageAuditRecordDTOS.stream().map(t -> TypeUtil.objToString(t.getUserId())).collect(Collectors.toList());
                stageDTO.setUsernames(users);
                stageDTO.setMultiAssign(users.size() > 1);
            }
            // 4.
            List<DevopsCdJobRecordDTO> jobRecordDTOList = devopsCdJobRecordService.queryByStageRecordId(stageRecordDTO.getId());
            List<DevopsPipelineTaskDTO> taskDTOList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(jobRecordDTOList)) {
                jobRecordDTOList.forEach(jobRecordDTO -> {
                    DevopsPipelineTaskDTO taskDTO = new DevopsPipelineTaskDTO();
                    taskDTO.setTaskRecordId(jobRecordDTO.getId());
                    taskDTO.setTaskName(jobRecordDTO.getName());
                    if (jobRecordDTO.getType().equals(JobTypeEnum.CD_AUDIT.value())) {
                        List<DevopsCdAuditRecordDTO> jobAuditRecordDTOS = devopsCdAuditRecordService.queryByJobRecordId(jobRecordDTO.getId());
                        if (CollectionUtils.isEmpty(jobAuditRecordDTOS)) {
                            throw new CommonException("error.audit.job.noUser");
                        }
                        List<String> taskUsers = jobAuditRecordDTOS.stream().map(t -> TypeUtil.objToString(t.getUserId())).collect(Collectors.toList());
                        taskDTO.setUsernames(taskUsers);
                        taskDTO.setMultiAssign(taskUsers.size() > 1);
                    }
                    taskDTO.setTaskType(jobRecordDTO.getType());
                    if (jobRecordDTO.getCountersigned() != null) {
                        taskDTO.setSign(jobRecordDTO.getCountersigned().longValue());
                    }
                    taskDTOList.add(taskDTO);
                });
            }
            stageDTO.setTasks(taskDTOList);
            // 5.
            if (i != stageRecordDTOList.size() - 1) {
                stageDTO.setNextStageTriggerType(stageRecordDTOList.get(i + 1).getTriggerType());
            }
        }
        devopsPipelineDTO.setStages(devopsPipelineStageDTOS);
        return devopsPipelineDTO;
    }
}
