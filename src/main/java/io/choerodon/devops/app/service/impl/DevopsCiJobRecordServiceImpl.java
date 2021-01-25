package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.JobWebHookVO;
import io.choerodon.devops.app.service.DevopsCiJobRecordService;
import io.choerodon.devops.app.service.DevopsCiJobService;
import io.choerodon.devops.app.service.DevopsCiPipelineRecordService;
import io.choerodon.devops.infra.dto.DevopsCiJobDTO;
import io.choerodon.devops.infra.dto.DevopsCiJobRecordDTO;
import io.choerodon.devops.infra.dto.DevopsCiPipelineRecordDTO;
import io.choerodon.devops.infra.dto.gitlab.JobDTO;
import io.choerodon.devops.infra.mapper.DevopsCiJobRecordMapper;
import io.choerodon.devops.infra.util.CiCdPipelineUtils;
import io.choerodon.devops.infra.util.TypeUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:27
 */
@Service
public class DevopsCiJobRecordServiceImpl implements DevopsCiJobRecordService {

    private static final String ERROR_GITLAB_JOB_ID_IS_NULL = "error.gitlab.job.id.is.null";
    private static final String ERROR_PIPELINE_RECORD_ID_IS_NULL = "error.pipeline.record.id.is.null";
    private static final String ERROR_PIPELINE_ID_IS_NULL = "error.pipeline.id.is.null";

    private DevopsCiJobRecordMapper devopsCiJobRecordMapper;
    private DevopsCiPipelineRecordService devopsCiPipelineRecordService;
    private DevopsCiJobService devopsCiJobService;

    public DevopsCiJobRecordServiceImpl(DevopsCiJobRecordMapper devopsCiJobRecordMapper,
                                        @Lazy DevopsCiPipelineRecordService devopsCiPipelineRecordService,
                                        @Lazy DevopsCiJobService devopsCiJobService) {
        this.devopsCiJobRecordMapper = devopsCiJobRecordMapper;
        this.devopsCiPipelineRecordService = devopsCiPipelineRecordService;
        this.devopsCiJobService = devopsCiJobService;
    }

    @Override
    public DevopsCiJobRecordDTO queryByGitlabJobId(Long gitlabJobId) {
        if (gitlabJobId == null) {
            throw new CommonException(ERROR_GITLAB_JOB_ID_IS_NULL);
        }
        DevopsCiJobRecordDTO devopsCiJobRecordDTO = new DevopsCiJobRecordDTO();
        devopsCiJobRecordDTO.setGitlabJobId(gitlabJobId);
        return devopsCiJobRecordMapper.selectOne(devopsCiJobRecordDTO);
    }

    @Override
    public void update(JobWebHookVO jobWebHookVO) {
        DevopsCiJobRecordDTO recordDTO = new DevopsCiJobRecordDTO();
        recordDTO.setGitlabJobId(jobWebHookVO.getBuildId());
        DevopsCiJobRecordDTO devopsCiJobRecordDTO = devopsCiJobRecordMapper.selectOne(recordDTO);
        if (devopsCiJobRecordDTO != null) {
            devopsCiJobRecordDTO.setStatus(jobWebHookVO.getBuildStatus());
            devopsCiJobRecordDTO.setStartedDate(jobWebHookVO.getBuildStartedAt());
            devopsCiJobRecordDTO.setFinishedDate(jobWebHookVO.getBuildFinishedAt());
            devopsCiJobRecordDTO.setDurationSeconds(jobWebHookVO.getBuildDuration());
            devopsCiJobRecordMapper.updateByPrimaryKeySelective(devopsCiJobRecordDTO);
        }
    }

    @Override
    @Transactional
    public void deleteByPipelineId(Long ciPipelineId) {
        if (ciPipelineId == null) {
            throw new CommonException(ERROR_PIPELINE_ID_IS_NULL);
        }
        // 查询流水线记录
        List<DevopsCiPipelineRecordDTO> devopsCiPipelineRecordDTOS = devopsCiPipelineRecordService.queryByPipelineId(ciPipelineId);
        if (!CollectionUtils.isEmpty(devopsCiPipelineRecordDTOS)) {
            devopsCiPipelineRecordDTOS.forEach(devopsCiPipelineRecordDTO -> {
                // 根据流水线记录id，删除job记录
                DevopsCiJobRecordDTO devopsCiJobRecordDTO = new DevopsCiJobRecordDTO();
                devopsCiJobRecordDTO.setCiPipelineRecordId(devopsCiPipelineRecordDTO.getId());
                devopsCiJobRecordMapper.delete(devopsCiJobRecordDTO);
            });
        }

    }

    @Override
    @Transactional
    public void deleteByGitlabProjectId(Long gitlabProjectId) {
        Objects.requireNonNull(gitlabProjectId);
        DevopsCiJobRecordDTO jobRecordDTO = new DevopsCiJobRecordDTO();
        jobRecordDTO.setGitlabProjectId(gitlabProjectId);
        devopsCiJobRecordMapper.delete(jobRecordDTO);
    }

    @Override
    public void create(Long ciPipelineRecordId, Long gitlabProjectId, List<JobDTO> jobDTOS, Long iamUserId) {
        jobDTOS.forEach(job -> create(ciPipelineRecordId, gitlabProjectId, job, iamUserId));
    }

    @Override
    public void create(Long ciPipelineRecordId, Long gitlabProjectId, JobDTO jobDTO, Long iamUserId) {
        DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordService.queryById(ciPipelineRecordId);
        List<DevopsCiJobDTO> devopsCiJobDTOS = devopsCiJobService.listByPipelineId(devopsCiPipelineRecordDTO.getCiPipelineId());
        Map<String, DevopsCiJobDTO> jobMap = devopsCiJobDTOS.stream().collect(Collectors.toMap(DevopsCiJobDTO::getName, v -> v));

        DevopsCiJobRecordDTO recordDTO = new DevopsCiJobRecordDTO();
        recordDTO.setCiPipelineRecordId(ciPipelineRecordId);
        recordDTO.setGitlabProjectId(gitlabProjectId);
        recordDTO.setStatus(jobDTO.getStatus().toValue());
        recordDTO.setStage(jobDTO.getStage());
        recordDTO.setGitlabJobId(TypeUtil.objToLong(jobDTO.getId()));
        recordDTO.setStartedDate(jobDTO.getStartedAt());
        recordDTO.setFinishedDate(jobDTO.getFinishedAt());
        recordDTO.setName(jobDTO.getName());
        recordDTO.setTriggerUserId(iamUserId);
        DevopsCiJobDTO existDevopsCiJobDTO = CiCdPipelineUtils.judgeAndGetJob(jobDTO.getName(), jobMap);
        if (!CollectionUtils.isEmpty(jobMap) && existDevopsCiJobDTO != null) {
            recordDTO.setType(existDevopsCiJobDTO.getType());
            recordDTO.setMetadata(existDevopsCiJobDTO.getMetadata());
        }

        devopsCiJobRecordMapper.insertSelective(recordDTO);
    }

    @Override
    public int selectCountByCiPipelineRecordId(Long ciPipelineRecordId) {
        DevopsCiJobRecordDTO condition = new DevopsCiJobRecordDTO();
        condition.setCiPipelineRecordId(Objects.requireNonNull(ciPipelineRecordId));
        return devopsCiJobRecordMapper.selectCount(condition);
    }
}
