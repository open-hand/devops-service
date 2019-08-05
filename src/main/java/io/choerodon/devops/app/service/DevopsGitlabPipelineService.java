package io.choerodon.devops.app.service;

import java.util.Date;
import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.infra.dto.DevopsGitlabPipelineDTO;

public interface DevopsGitlabPipelineService {

    void create(PipelineWebHookVO pipelineWebHookVO, String token);

    void updateStages(JobWebHookVO jobWebHookVO);

    PipelineTimeVO getPipelineTime(Long appId, Date startTime, Date endTime);

    PipelineFrequencyVO getPipelineFrequency(Long appId, Date startTime, Date endTime);

    void handleCreate(PipelineWebHookVO pipelineWebHookVO);

    PageInfo<DevopsGitlabPipelineVO> pageByOptions(Long appId, String branch, PageRequest pageRequest, Date startTime, Date endTime);

    void baseCreate(DevopsGitlabPipelineDTO devopsGitlabPipelineDTO);

    DevopsGitlabPipelineDTO baseQueryByGitlabPipelineId(Long id);

    void baseUpdate(DevopsGitlabPipelineDTO devopsGitlabPipelineDTO);

    DevopsGitlabPipelineDTO baseQueryByCommitId(Long commitId);

    List<DevopsGitlabPipelineDTO> baseListByApplicationId(Long appId, Date startTime, Date endTime);

    PageInfo<DevopsGitlabPipelineDTO> basePageByApplicationId(Long appId, PageRequest pageRequest, Date startTime, Date endTime);

    void baseDeleteWithoutCommit();

    List<DevopsGitlabPipelineDTO> baseListByAppIdAndBranch(Long appId, String branch);
}
