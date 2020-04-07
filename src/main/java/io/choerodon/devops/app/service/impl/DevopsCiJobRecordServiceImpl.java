package io.choerodon.devops.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.JobWebHookVO;
import io.choerodon.devops.app.service.DevopsCiJobRecordService;
import io.choerodon.devops.infra.dto.DevopsCiJobRecordDTO;
import io.choerodon.devops.infra.mapper.DevopsCiJobRecordMapper;
import org.springframework.stereotype.Service;

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

    private DevopsCiJobRecordMapper devopsCiJobRecordMapper;

    public DevopsCiJobRecordServiceImpl(DevopsCiJobRecordMapper devopsCiJobRecordMapper) {
        this.devopsCiJobRecordMapper = devopsCiJobRecordMapper;
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
}
