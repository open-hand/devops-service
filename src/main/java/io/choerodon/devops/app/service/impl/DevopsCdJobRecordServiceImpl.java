package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.alibaba.fastjson.TypeReference;
import org.apache.commons.beanutils.ConvertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.asgard.saga.annotation.SagaTask;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.CdApiTestConfigForSagaVO;
import io.choerodon.devops.api.vo.CdApiTestConfigVO;
import io.choerodon.devops.api.vo.PodEventVO;
import io.choerodon.devops.api.vo.host.CommandResultVO;
import io.choerodon.devops.app.eventhandler.constants.SagaTaskCodeConstants;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.DevopsCdJobRecordDTO;
import io.choerodon.devops.infra.dto.PortMapVO;
import io.choerodon.devops.infra.enums.JobTypeEnum;
import io.choerodon.devops.infra.enums.PipelineStatus;
import io.choerodon.devops.infra.mapper.DevopsCdJobRecordMapper;
import io.choerodon.devops.infra.util.JsonHelper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/3 13:47
 */
@Service
public class DevopsCdJobRecordServiceImpl implements DevopsCdJobRecordService {
    public static final Logger LOGGER = LoggerFactory.getLogger(DevopsCdJobRecordServiceImpl.class);

    private static final String ERROR_SAVE_JOB_RECORD_FAILED = "error.save.job.record.failed";
    private static final String ERROR_UPDATE_JOB_RECORD_FAILED = "error.update.job.record.failed";
    private static final Long DEFAULT_JOB_DURATION_SECONDS = 1L;
    @Autowired
    private DevopsCdJobRecordMapper devopsCdJobRecordMapper;
    @Autowired
    @Lazy
    private DevopsCdStageRecordService devopsCdStageRecordService;
    @Autowired
    @Lazy
    private DevopsCdPipelineRecordService devopsCdPipelineRecordService;
    @Autowired
    private DevopsCdAuditRecordService devopsCdAuditRecordService;
    @Lazy
    @Autowired
    private DevopsCdPipelineService devopsCdPipelineService;
    @Autowired
    private DevopsEnvResourceService devopsEnvResourceService;

    @Override
    public List<DevopsCdJobRecordDTO> queryByStageRecordId(Long stageRecordId) {
        DevopsCdJobRecordDTO jobRecordDTO = new DevopsCdJobRecordDTO();
        jobRecordDTO.setStageRecordId(stageRecordId);
        return devopsCdJobRecordMapper.select(jobRecordDTO);
    }

    @Override
    @Transactional
    public void save(DevopsCdJobRecordDTO devopsCdJobRecordDTO) {
        if (devopsCdJobRecordMapper.insert(devopsCdJobRecordDTO) != 1) {
            throw new CommonException(ERROR_SAVE_JOB_RECORD_FAILED);
        }
    }

    @Override
    public DevopsCdJobRecordDTO queryFirstByStageRecordId(Long stageRecordId) {
        return devopsCdJobRecordMapper.queryFirstByStageRecordId(stageRecordId);
    }

    @Override
    @Transactional
    public void update(DevopsCdJobRecordDTO devopsCdJobRecordDTO) {
        if (devopsCdJobRecordMapper.updateByPrimaryKeySelective(devopsCdJobRecordDTO) != 1) {
            throw new CommonException(ERROR_UPDATE_JOB_RECORD_FAILED);
        }
    }

    @Override
    @Transactional
    public void updateStatusById(Long jobRecordId, String status) {
        DevopsCdJobRecordDTO cdJobRecordDTO = devopsCdJobRecordMapper.selectByPrimaryKey(jobRecordId);
        if (status.equals(PipelineStatus.FAILED.toValue())
                || status.equals(PipelineStatus.SUCCESS.toValue())
                || status.equals(PipelineStatus.STOP.toValue())) {
            cdJobRecordDTO.setFinishedDate(new Date());
            if (cdJobRecordDTO.getStartedDate() != null) {

                long timeSeconds = (new Date().getTime() - cdJobRecordDTO.getStartedDate().getTime()) / 1000;
                //执行时间太短为0s，则取1s
                cdJobRecordDTO.setDurationSeconds(timeSeconds > 0 ? timeSeconds : 1L);
            }
        }
        if (status.equals(PipelineStatus.RUNNING.toValue())) {
            cdJobRecordDTO.setStartedDate(new Date());
            cdJobRecordDTO.setFinishedDate(null);
        }

        // 已取消的任务 不能更新为成功、失败状态
        if (cdJobRecordDTO.getStatus().equals(PipelineStatus.CANCELED.toValue())
                && (status.equals(PipelineStatus.FAILED.toValue())
                || status.equals(PipelineStatus.SUCCESS.toValue()))) {
            LOGGER.info("cancel job can not update status!! job record Id {}", cdJobRecordDTO.getId());
            return;
        }

        cdJobRecordDTO.setStatus(status);
        if (devopsCdJobRecordMapper.updateByPrimaryKey(cdJobRecordDTO) != 1) {
            throw new CommonException(ERROR_UPDATE_JOB_RECORD_FAILED);
        }
    }

    @Override
    @Transactional
    public void updateLogById(Long jobRecordId, StringBuilder log) {
        DevopsCdJobRecordDTO cdJobRecordDTO = devopsCdJobRecordMapper.selectByPrimaryKey(jobRecordId);
        cdJobRecordDTO.setLog(log.toString());
        if (devopsCdJobRecordMapper.updateByPrimaryKey(cdJobRecordDTO) != 1) {
            throw new CommonException(ERROR_UPDATE_JOB_RECORD_FAILED);
        }
    }

    @Override
    public String getHostLogById(Long jobRecordId) {
        return devopsCdJobRecordMapper.selectByPrimaryKey(jobRecordId).getLog();
    }

    @Override
    public DevopsCdJobRecordDTO queryById(Long id) {
        Assert.notNull(id, PipelineCheckConstant.ERROR_JOB_RECORD_ID_IS_NULL);
        return devopsCdJobRecordMapper.selectByPrimaryKey(id);
    }

    @Override
    @Transactional
    public void updateJobStatusFailed(Long jobRecordId, String log) {
        DevopsCdJobRecordDTO devopsCdJobRecordDTO = queryById(jobRecordId);
        devopsCdJobRecordDTO.setStatus(PipelineStatus.FAILED.toValue());
        devopsCdJobRecordDTO.setFinishedDate(new Date());
        devopsCdJobRecordDTO.setLog(log);
        if (devopsCdJobRecordDTO.getStartedDate() != null) {
            devopsCdJobRecordDTO.setDurationSeconds((new Date().getTime() - devopsCdJobRecordDTO.getStartedDate().getTime()) / 1000);
        } else {
            devopsCdJobRecordDTO.setDurationSeconds(DEFAULT_JOB_DURATION_SECONDS);
        }
        update(devopsCdJobRecordDTO);
    }

    @Override
    @Transactional
    public void updateJobStatusNotAudit(Long pipelineRecordId, Long stageRecordId, Long jobRecordId) {
        DevopsCdJobRecordDTO devopsCdJobRecordDTO = queryById(jobRecordId);
        // 更新job状态为待审核
        devopsCdJobRecordDTO.setStartedDate(new Date());
        devopsCdJobRecordDTO.setStatus(PipelineStatus.NOT_AUDIT.toValue());
        update(devopsCdJobRecordDTO);
        // 更新阶段状态为待审核
        devopsCdStageRecordService.updateStatusById(stageRecordId, PipelineStatus.NOT_AUDIT.toValue());
        // 同时更新流水线状态为待审核
        devopsCdPipelineRecordService.updateStatusById(pipelineRecordId, PipelineStatus.NOT_AUDIT.toValue());
        // 通知审核人员
        devopsCdAuditRecordService.sendJobAuditMessage(pipelineRecordId, devopsCdJobRecordDTO);
    }

    @Override
    @Transactional
    public void retryCdJob(Long projectId, Long pipelineRecordId, Long stageRecordId, Long jobRecordId) {
        DevopsCdJobRecordDTO devopsCdJobRecordDTO = devopsCdJobRecordMapper.selectByPrimaryKey(jobRecordId);
        // 1. 更新流水线状态为执行中
        devopsCdPipelineRecordService.updateStatusById(pipelineRecordId, PipelineStatus.RUNNING.toValue());
        // 2. 更新阶段状态为执行中
        devopsCdStageRecordService.updateStatusById(stageRecordId, PipelineStatus.RUNNING.toValue());
        // 3. 重试任务
        if (JobTypeEnum.CD_DEPLOY.value().equals(devopsCdJobRecordDTO.getType())
                || JobTypeEnum.CD_DEPLOYMENT.value().equals(devopsCdJobRecordDTO.getType())) {
            // 4.1 重试环境部署任务
            devopsCdPipelineService.envAutoDeploy(pipelineRecordId, stageRecordId, jobRecordId);
        } else if (JobTypeEnum.CD_HOST.value().equals(devopsCdJobRecordDTO.getType())) {
            devopsCdPipelineRecordService.retryHostDeployJob(pipelineRecordId, stageRecordId, jobRecordId);
        }
    }

    @Override
    @Transactional
    public void updateJobStatusSuccess(Long jobRecordId) {
        DevopsCdJobRecordDTO devopsCdJobRecordDTO = queryById(jobRecordId);
        devopsCdJobRecordDTO.setStatus(PipelineStatus.SUCCESS.toValue());
        devopsCdJobRecordDTO.setFinishedDate(new Date());
        if (devopsCdJobRecordDTO.getStartedDate() != null) {
            devopsCdJobRecordDTO.setDurationSeconds((new Date().getTime() - devopsCdJobRecordDTO.getStartedDate().getTime()) / 1000);
        }
        update(devopsCdJobRecordDTO);
    }

    @Override
    @Transactional
    public void updateJobStatusStopByStageRecordId(Long stageRecordId) {
        List<DevopsCdJobRecordDTO> devopsCdJobRecordDTOS = queryByStageRecordId(stageRecordId);
        devopsCdJobRecordDTOS.forEach(jobrecord -> {
            jobrecord.setStatus(PipelineStatus.STOP.toValue());
            if (devopsCdJobRecordMapper.updateByPrimaryKeySelective(jobrecord) != 1) {
                throw new CommonException(ERROR_UPDATE_JOB_RECORD_FAILED);
            }
        });
    }

    @Override
    public List<PodEventVO> queryDeployJobLogs(Long projectId, Long pipelineRecordId, Long stageRecordId, Long jobRecordId) {
        DevopsCdJobRecordDTO devopsCdJobRecordDTO = queryById(jobRecordId);
        return devopsEnvResourceService.listPodEventBycommandId(devopsCdJobRecordDTO.getCommandId());
    }

    @Override
    @Transactional
    public void updateNotAuditJobStatus(Long jobRecordId, String status) {
        if (devopsCdJobRecordMapper.updateNotAuditJobStatus(jobRecordId, status) != 1) {
            throw new CommonException(ERROR_UPDATE_JOB_RECORD_FAILED);
        }
    }

    @Override
    public List<DevopsCdJobRecordDTO> queryJobWithStageRecordIdAndStatus(Long stageRecordId, String status) {
        Assert.notNull(stageRecordId, PipelineCheckConstant.ERROR_STAGE_RECORD_ID_IS_NULL);
        Assert.notNull(status, PipelineCheckConstant.ERROR_JOB_STATUS_IS_NULL);

        DevopsCdJobRecordDTO devopsCdJobRecordDTO = new DevopsCdJobRecordDTO();
        devopsCdJobRecordDTO.setStageRecordId(stageRecordId);
        devopsCdJobRecordDTO.setStatus(status);
        return devopsCdJobRecordMapper.select(devopsCdJobRecordDTO);
    }

    @Override
    public DevopsCdJobRecordDTO queryByPipelineRecordIdAndJobName(Long pipelineRecordId, String deployJobName) {
        return devopsCdJobRecordMapper.queryByPipelineRecordIdAndJobName(pipelineRecordId, deployJobName);
    }

    @Override
    public List<CdApiTestConfigForSagaVO> listCdApiTestConfig() {
        List<CdApiTestConfigForSagaVO> cdApiTestConfigForSagaVOArrayList = new ArrayList<>();
        List<DevopsCdJobRecordDTO> devopsCdJobRecordDTOList = devopsCdJobRecordMapper.listByType(JobTypeEnum.CD_API_TEST.value());
        if (!CollectionUtils.isEmpty(devopsCdJobRecordDTOList)) {
            devopsCdJobRecordDTOList.forEach(devopsCdJobRecordDTO -> {
                CdApiTestConfigVO cdApiTestConfigVO = JsonHelper.unmarshalByJackson(devopsCdJobRecordDTO.getMetadata(), CdApiTestConfigVO.class);
                CdApiTestConfigForSagaVO cdApiTestConfigForSagaVO = new CdApiTestConfigForSagaVO();
                ConvertUtils.convert(cdApiTestConfigVO, CdApiTestConfigForSagaVO.class);
                cdApiTestConfigForSagaVO.setDevopsCdJobRecordId(devopsCdJobRecordDTO.getId());
                cdApiTestConfigForSagaVOArrayList.add(cdApiTestConfigForSagaVO);
            });
        }
        return cdApiTestConfigForSagaVOArrayList;
    }

    @SagaTask(sagaCode = SagaTopicCodeConstants.REPAIR_API_TEST_TASK_FROM_CD,
            code = SagaTaskCodeConstants.DEVOPS_REPAIR_API_TEST_TASK_FROM_CD,
            seq = 10,
            maxRetryCount = 3)
    @Override
    public void taskRepairCdJobRecordData(String payload) {
        List<CdApiTestConfigForSagaVO> cdApiTestConfigForSagaVOList = JsonHelper.unmarshalByJackson(payload, new com.fasterxml.jackson.core.type.TypeReference<List<CdApiTestConfigForSagaVO>>() {
        });
        repairCdJobRecordData(cdApiTestConfigForSagaVOList);
    }

    private void repairCdJobRecordData(List<CdApiTestConfigForSagaVO> cdApiTestConfigForSagaVOList) {
        if (!CollectionUtils.isEmpty(cdApiTestConfigForSagaVOList)) {
            LOGGER.info(">>>>>>>>>>> start fix devops_cd_job_record from pipeline cd api test <<<<<<<<<<<<<<<<<<<<");
            //记录处理失败的devopsCdJobRecordId
            Set<Long> errorSet = new HashSet<>();
            cdApiTestConfigForSagaVOList.forEach(cdApiTestConfigForSagaVO -> {
                DevopsCdJobRecordDTO devopsCdJobRecordDTO = devopsCdJobRecordMapper.selectByPrimaryKey(cdApiTestConfigForSagaVO.getDevopsCdJobRecordId());
                try {
                    CdApiTestConfigVO cdApiTestConfigVO = JsonHelper.unmarshalByJackson(devopsCdJobRecordDTO.getMetadata(), CdApiTestConfigVO.class);
                    cdApiTestConfigVO.setApITestConfigId(cdApiTestConfigForSagaVO.getApiTestConfigId());
                    devopsCdJobRecordDTO.setMetadata(JsonHelper.marshalByJackson(cdApiTestConfigVO));
                    devopsCdJobRecordMapper.updateByPrimaryKeySelective(devopsCdJobRecordDTO);
                } catch (Exception e) {
                    errorSet.add(devopsCdJobRecordDTO.getId());
                    LOGGER.info(">>>>>>>>>>> update to devops_cd_job_record failed! devopsCdJobRecordId: {} <<<<<<<<<<<<<<<<<<<<", devopsCdJobRecordDTO.getId());
                }
            });
            if (CollectionUtils.isEmpty(errorSet)) {
                LOGGER.info(">>>>>>>>>>> end fix devops_cd_job_record <<<<<<<<<<<<<<<<<<<<");
            } else {
                errorSet.forEach(error -> LOGGER.info(">>>>>>>>>>> end fix devops_cd_job_record, but exist error. failed devopsCdJobRecordId is: {} <<<<<<<<<<<<<<<<<<<<", error));
            }
            LOGGER.info(">>>>>>>>>>> end fix devops_cd_job_record from pipeline cd api test <<<<<<<<<<<<<<<<<<<<");
        }
    }

}
