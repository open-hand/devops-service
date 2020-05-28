package io.choerodon.devops.app.service;

import java.util.Date;
import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.infra.dto.DevopsGitlabPipelineDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

public interface DevopsGitlabPipelineService {

    void create(PipelineWebHookVO pipelineWebHookVO, String token);

    void updateStages(JobWebHookVO jobWebHookVO);

    PipelineTimeVO getPipelineTime(Long appServiceId, Date startTime, Date endTime);

    PipelineFrequencyVO getPipelineFrequency(Long appServiceId, Date startTime, Date endTime);

    void handleCreate(PipelineWebHookVO pipelineWebHookVO);

    Page<DevopsGitlabPipelineVO> pageByOptions(Long appServiceId, String branch, PageRequest pageable, Date startTime, Date endTime);

    void baseCreate(DevopsGitlabPipelineDTO devopsGitlabPipelineDTO);

    DevopsGitlabPipelineDTO baseQueryByGitlabPipelineId(Long id);

    void baseUpdate(DevopsGitlabPipelineDTO devopsGitlabPipelineDTO);

    DevopsGitlabPipelineDTO baseQueryByCommitId(Long commitId);

    List<DevopsGitlabPipelineDTO> baseListByApplicationId(Long appServiceId, Date startTime, Date endTime);

    Page<DevopsGitlabPipelineDTO> basePageByApplicationId(Long appServiceId, PageRequest pageable, Date startTime, Date endTime);

    void baseDeleteWithoutCommit();

    List<DevopsGitlabPipelineDTO> baseListByAppIdAndBranch(Long appServiceId, String branch);
}
