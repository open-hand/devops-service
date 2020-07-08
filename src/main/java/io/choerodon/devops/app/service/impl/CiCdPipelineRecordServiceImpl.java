package io.choerodon.devops.app.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.CiCdPipelineRecordVO;
import io.choerodon.devops.api.vo.DevopsCdPipelineRecordVO;
import io.choerodon.devops.api.vo.DevopsCiPipelineRecordVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.DevopsCdJobRecordDTO;
import io.choerodon.devops.infra.dto.DevopsCdPipelineRecordDTO;
import io.choerodon.devops.infra.dto.DevopsCdStageRecordDTO;
import io.choerodon.devops.infra.dto.workflow.DevopsPipelineDTO;
import io.choerodon.devops.infra.enums.PipelineStatus;
import io.choerodon.devops.infra.enums.WorkFlowStatus;
import io.choerodon.devops.infra.mapper.DevopsCdJobRecordMapper;
import io.choerodon.devops.infra.mapper.DevopsCdStageRecordMapper;
import io.choerodon.devops.infra.util.GenerateUUID;
import io.choerodon.devops.infra.util.GitUserNameUtil;

@Service
public class CiCdPipelineRecordServiceImpl implements CiCdPipelineRecordService {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Autowired
    private DevopsCiPipelineRecordService devopsCiPipelineRecordService;

    @Autowired
    private DevopsCdPipelineRecordService devopsCdPipelineRecordService;

    @Autowired
    private DevopsCdStageRecordMapper devopsCdStageRecordMapper;
    @Autowired
    private DevopsCdJobRecordMapper devopsCdJobRecordMapper;
    @Autowired
    @Lazy
    private DevopsCdJobRecordService devopsCdJobRecordService;
    @Autowired
    private DevopsCdStageRecordService devopsCdStageRecordService;

    @Autowired
    @Lazy
    private DevopsCdPipelineService devopsCdPipelineService;



    @Override
    public CiCdPipelineRecordVO queryPipelineRecordDetails(Long projectId, Long gitlabPipelineId, Long pipelioneRecordId) {
        CiCdPipelineRecordVO ciCdPipelineRecordVO = new CiCdPipelineRecordVO();
        DevopsCiPipelineRecordVO devopsCiPipelineRecordVO = devopsCiPipelineRecordService.queryPipelineRecordDetails(projectId, gitlabPipelineId);
        ciCdPipelineRecordVO.setCiPipelineRecordVO(devopsCiPipelineRecordVO);
        DevopsCdPipelineRecordVO devopsCdPipelineRecordVO = devopsCdPipelineRecordService.queryPipelineRecordDetails(projectId, gitlabPipelineId);
        ciCdPipelineRecordVO.setCdPipelineRecordVO(devopsCdPipelineRecordVO);
        return ciCdPipelineRecordVO;
    }

    @Override
    public void retryPipeline(Long projectId, Long pipelineRecordId, Long gitlabPipelineId, Long gitlabProjectId) {
        DevopsCdStageRecordDTO cdStageRecordDTO = devopsCdStageRecordMapper.queryFailedOrCancelStage(pipelineRecordId);
        if (cdStageRecordDTO == null || cdStageRecordDTO.getId() == null) {
            devopsCiPipelineRecordService.retry(projectId, gitlabPipelineId, gitlabProjectId);
        } else {
            retryCdPipeline(projectId, pipelineRecordId);
        }
    }

    @Transactional
    public void retryCdPipeline(Long projectId, Long pipelineRecordId) {
        // 0.1 更新business key
        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = devopsCdPipelineRecordService.queryById(pipelineRecordId);
        devopsCdPipelineRecordDTO.setBusinessKey(GenerateUUID.generateUUID());
        devopsCdPipelineRecordDTO.setStatus(PipelineStatus.RUNNING.toValue());
        devopsCdPipelineRecordService.update(devopsCdPipelineRecordDTO);
        // 1. 根据装填获取DevopsPipelineDTO
        DevopsPipelineDTO devopsPipelineDTO = devopsCdPipelineRecordService.createCDWorkFlowDTO(pipelineRecordId, true);
        // 2.更新状态
        DevopsCdStageRecordDTO cdStageRecordDTO = devopsCdStageRecordMapper.queryFailedOrCancelStage(pipelineRecordId);
        DevopsCdJobRecordDTO cdJobRecordDTO = devopsCdJobRecordMapper.queryFailedOrCancelJob(cdStageRecordDTO.getId());
        devopsCdStageRecordService.updateStatusById(cdJobRecordDTO.getId(), PipelineStatus.RUNNING.toValue());
        devopsCdJobRecordService.updateStatusById(cdJobRecordDTO.getId(), PipelineStatus.RUNNING.toValue());

        try {
            CustomUserDetails details = DetailsHelper.getUserDetails();
            // 执行流水线
            devopsCdPipelineService.createWorkFlow(projectId, devopsPipelineDTO, details.getUsername(), details.getUserId(), details.getOrganizationId());
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            devopsCdPipelineRecordDTO.setStatus(WorkFlowStatus.FAILED.toValue());
            devopsCdPipelineRecordDTO.setErrorInfo(e.getMessage());
            devopsCdPipelineRecordService.update(devopsCdPipelineRecordDTO);
        }
    }
}
