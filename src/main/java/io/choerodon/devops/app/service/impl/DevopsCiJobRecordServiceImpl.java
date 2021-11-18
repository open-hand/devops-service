package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.JobWebHookVO;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.DevopsCiJobRecordService;
import io.choerodon.devops.app.service.DevopsCiJobService;
import io.choerodon.devops.app.service.DevopsCiPipelineRecordService;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.gitlab.JobDTO;
import io.choerodon.devops.infra.mapper.DevopsCiJobRecordMapper;
import io.choerodon.devops.infra.mapper.DevopsCiMavenSettingsMapper;
import io.choerodon.devops.infra.util.CiCdPipelineUtils;
import io.choerodon.devops.infra.util.JsonHelper;
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
    private static final String ERROR_PIPELINE_ID_IS_NULL = "error.pipeline.id.is.null";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private DevopsCiJobRecordMapper devopsCiJobRecordMapper;
    private DevopsCiPipelineRecordService devopsCiPipelineRecordService;
    private DevopsCiJobService devopsCiJobService;
    @Autowired
    private DevopsCiMavenSettingsMapper devopsCiMavenSettingsMapper;
    @Autowired
    private AppServiceService appServiceService;

    public DevopsCiJobRecordServiceImpl(DevopsCiJobRecordMapper devopsCiJobRecordMapper,
                                        @Lazy DevopsCiPipelineRecordService devopsCiPipelineRecordService,
                                        @Lazy DevopsCiJobService devopsCiJobService) {
        this.devopsCiJobRecordMapper = devopsCiJobRecordMapper;
        this.devopsCiPipelineRecordService = devopsCiPipelineRecordService;
        this.devopsCiJobService = devopsCiJobService;
    }

    @Override
    public DevopsCiJobRecordDTO queryByAppServiceIdAndGitlabJobId(Long appServiceId, Long gitlabJobId) {
        Assert.notNull(appServiceId, ResourceCheckConstant.ERROR_APP_SERVICE_ID_IS_NULL);
        Assert.notNull(gitlabJobId, ERROR_GITLAB_JOB_ID_IS_NULL);

        DevopsCiJobRecordDTO devopsCiJobRecordDTO = new DevopsCiJobRecordDTO();
        devopsCiJobRecordDTO.setGitlabJobId(gitlabJobId);
        devopsCiJobRecordDTO.setAppServiceId(appServiceId);
        return devopsCiJobRecordMapper.selectOne(devopsCiJobRecordDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(JobWebHookVO jobWebHookVO, String token) {
        AppServiceDTO appServiceDTO = appServiceService.baseQueryByToken(token);
        DevopsCiJobRecordDTO devopsCiJobRecordDTO = queryByAppServiceIdAndGitlabJobId(appServiceDTO.getId(), jobWebHookVO.getBuildId());
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
    public void create(Long ciPipelineRecordId, Long gitlabProjectId, List<JobDTO> jobDTOS, Long iamUserId, Long appServiceId) {
        jobDTOS.forEach(job -> create(ciPipelineRecordId, gitlabProjectId, job, iamUserId, appServiceId));
    }

    @Override
    public void create(Long ciPipelineRecordId, Long gitlabProjectId, JobDTO jobDTO, Long iamUserId, Long appServiceId) {
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
        recordDTO.setAppServiceId(appServiceId);
        DevopsCiJobDTO existDevopsCiJobDTO = CiCdPipelineUtils.judgeAndGetJob(jobDTO.getName(), jobMap);
        if (!CollectionUtils.isEmpty(jobMap) && existDevopsCiJobDTO != null) {
            recordDTO.setType(existDevopsCiJobDTO.getType());
            recordDTO.setMetadata(existDevopsCiJobDTO.getMetadata());
            DevopsCiMavenSettingsDTO devopsCiMavenSettingsDTO = new DevopsCiMavenSettingsDTO();
            devopsCiMavenSettingsDTO.setCiJobId(existDevopsCiJobDTO.getId());
            DevopsCiMavenSettingsDTO ciMavenSettingsDTO = devopsCiMavenSettingsMapper.selectOne(devopsCiMavenSettingsDTO);
            if (!Objects.isNull(ciMavenSettingsDTO)) {
                recordDTO.setMavenSettingId(ciMavenSettingsDTO.getId());
            }

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
