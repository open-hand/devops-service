package io.choerodon.devops.app.service.impl;

import java.util.List;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.JobWebHookVO;
import io.choerodon.devops.app.service.DevopsCiJobRecordService;
import io.choerodon.devops.app.service.DevopsCiPipelineRecordService;
import io.choerodon.devops.infra.dto.DevopsCiJobRecordDTO;
import io.choerodon.devops.infra.dto.DevopsCiPipelineRecordDTO;
import io.choerodon.devops.infra.mapper.DevopsCiJobRecordMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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

    public DevopsCiJobRecordServiceImpl(DevopsCiJobRecordMapper devopsCiJobRecordMapper,
                                        @Lazy DevopsCiPipelineRecordService devopsCiPipelineRecordService) {
        this.devopsCiJobRecordMapper = devopsCiJobRecordMapper;
        this.devopsCiPipelineRecordService = devopsCiPipelineRecordService;
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
}
