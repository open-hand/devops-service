package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.CiCdPipelineRecordVO;
import io.choerodon.devops.api.vo.DevopsCdPipelineRecordVO;
import io.choerodon.devops.api.vo.DevopsCiPipelineRecordVO;
import io.choerodon.devops.api.vo.DevopsCiPipelineVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.gitlab.CommitDTO;
import io.choerodon.devops.infra.dto.workflow.DevopsPipelineDTO;
import io.choerodon.devops.infra.enums.PipelineStatus;
import io.choerodon.devops.infra.enums.WorkFlowStatus;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.WorkFlowServiceOperator;
import io.choerodon.devops.infra.mapper.*;
import io.choerodon.devops.infra.util.GenerateUUID;
import io.choerodon.devops.infra.util.GitUserNameUtil;
import io.choerodon.devops.infra.util.TypeUtil;

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

    @Autowired
    private WorkFlowServiceOperator workFlowServiceOperator;

    @Autowired
    private DevopsCiPipelineService devopsCiPipelineService;

    @Autowired
    private DevopsCiStageMapper devopsCiStageMapper;

    @Autowired
    private AppServiceMapper appServiceMapper;

    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;


    @Override
    public CiCdPipelineRecordVO queryPipelineRecordDetails(Long projectId, Long gitlabPipelineId) {
        CiCdPipelineRecordVO ciCdPipelineRecordVO = new CiCdPipelineRecordVO();
        DevopsCiPipelineRecordVO devopsCiPipelineRecordVO = devopsCiPipelineRecordService.queryPipelineRecordDetails(projectId, gitlabPipelineId);
        ciCdPipelineRecordVO.setCiPipelineRecordVO(devopsCiPipelineRecordVO);
        DevopsCdPipelineRecordVO devopsCdPipelineRecordVO = devopsCdPipelineRecordService.queryPipelineRecordDetails(projectId, gitlabPipelineId);
        ciCdPipelineRecordVO.setCdPipelineRecordVO(devopsCdPipelineRecordVO);
        return ciCdPipelineRecordVO;
    }

    @Override
    public void retryPipeline(Long projectId, Long cdPipelineRecordId, Long gitlabPipelineId, Long gitlabProjectId) {
        DevopsCdStageRecordDTO cdStageRecordDTO = devopsCdStageRecordMapper.queryFailedOrCancelStage(cdPipelineRecordId);
        if (cdStageRecordDTO == null || cdStageRecordDTO.getId() == null) {
            devopsCiPipelineRecordService.retry(projectId, gitlabPipelineId, gitlabProjectId);
        } else {
            retryCdPipeline(projectId, cdPipelineRecordId);
        }
    }

    @Transactional
    public void retryCdPipeline(Long projectId, Long cdPipelineRecordId) {
        // 0.1 更新business key
        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = devopsCdPipelineRecordService.queryById(cdPipelineRecordId);
        devopsCdPipelineRecordDTO.setBusinessKey(GenerateUUID.generateUUID());
        devopsCdPipelineRecordDTO.setStatus(PipelineStatus.RUNNING.toValue());
        devopsCdPipelineRecordService.update(devopsCdPipelineRecordDTO);
        // 1. 根据装填获取DevopsPipelineDTO
        DevopsPipelineDTO devopsPipelineDTO = devopsCdPipelineRecordService.createCDWorkFlowDTO(cdPipelineRecordId, true);
        // 2.更新状态
        DevopsCdStageRecordDTO cdStageRecordDTO = devopsCdStageRecordMapper.queryFailedOrCancelStage(cdPipelineRecordId);
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

    @Override
    public void cancel(Long projectId, Long cdPipelineRecordId, Long gitlabPipelineId, Long gitlabProjectId) {
        DevopsCdStageRecordDTO cdStageRecordDTO = devopsCdStageRecordMapper.queryPendingAndRunning(cdPipelineRecordId);
        if (cdStageRecordDTO == null || cdStageRecordDTO.getId() == null) {
            devopsCiPipelineRecordService.cancel(projectId, gitlabPipelineId, gitlabProjectId);
        } else {
            cancelCdPipeline(cdPipelineRecordId);
        }
    }


    @Override
    public void executeNew(Long projectId, Long pipelineId, Long gitlabProjectId, String ref) {
        DevopsCiStageDTO devopsCdStageDTO = new DevopsCiStageDTO();
        devopsCdStageDTO.setCiPipelineId(pipelineId);
        if (devopsCiStageMapper.selectCount(devopsCdStageDTO) == 0) {
            DevopsCiPipelineVO devopsCiPipelineDTO = devopsCiPipelineService.queryById(pipelineId);
            AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(devopsCiPipelineDTO.getAppServiceId());
            List<CommitDTO> commitDTOList = gitlabServiceClientOperator.getCommits(TypeUtil.objToInteger(gitlabProjectId), ref, null);
            if (CollectionUtils.isEmpty(commitDTOList)) {
                throw new CommonException("error.ref.no.commit");
            }
            devopsCdPipelineService.triggerCdPipeline(appServiceDTO.getToken(), commitDTOList.get(0).getId(), ref, null);
        } else {
            devopsCiPipelineService.executeNew(projectId, pipelineId, gitlabProjectId, ref);
        }
    }

    @Transactional
    public void cancelCdPipeline(Long pipelineRecordId) {
        DevopsCdPipelineRecordDTO pipelineRecordDTO = devopsCdPipelineRecordService.queryById(pipelineRecordId);

        DevopsCdStageRecordDTO cdStageRecordDTO = devopsCdStageRecordMapper.queryPendingAndRunning(pipelineRecordId);
        DevopsCdJobRecordDTO cdJobRecordDTO = devopsCdJobRecordMapper.queryPendingAndRunning(cdStageRecordDTO.getId());
        devopsCdStageRecordService.updateStatusById(cdJobRecordDTO.getId(), PipelineStatus.CANCELED.toValue());
        devopsCdJobRecordService.updateStatusById(cdJobRecordDTO.getId(), PipelineStatus.CANCELED.toValue());
        devopsCdPipelineRecordService.updateStatusById(pipelineRecordId, PipelineStatus.CANCELED.toValue());

        workFlowServiceOperator.stopInstance(pipelineRecordDTO.getProjectId(), pipelineRecordDTO.getBusinessKey());
    }
}
