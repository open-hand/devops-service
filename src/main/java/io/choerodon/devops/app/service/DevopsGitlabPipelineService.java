package io.choerodon.devops.app.service;

import java.util.Date;
import java.util.List;

import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.infra.dto.DevopsGitlabPipelineDTO;

public interface DevopsGitlabPipelineService {

    void create(PipelineWebHookVO pipelineWebHookVO, String token);

    void updateStages(JobWebHookVO jobWebHookVO);

    PipelineTimeVO getPipelineTime(Long appServiceId, Date startTime, Date endTime);

    PipelineFrequencyVO getPipelineFrequency(Long appServiceId, Date startTime, Date endTime);

    void handleCreate(PipelineWebHookVO pipelineWebHookVO);

    PageInfo<DevopsGitlabPipelineVO> pageByOptions(Long appServiceId, String branch, Pageable pageable, Date startTime, Date endTime);

    void baseCreate(DevopsGitlabPipelineDTO devopsGitlabPipelineDTO);

    DevopsGitlabPipelineDTO baseQueryByGitlabPipelineId(Long id);

    void baseUpdate(DevopsGitlabPipelineDTO devopsGitlabPipelineDTO);

    DevopsGitlabPipelineDTO baseQueryByCommitId(Long commitId);

    List<DevopsGitlabPipelineDTO> baseListByApplicationId(Long appServiceId, Date startTime, Date endTime);

    PageInfo<DevopsGitlabPipelineDTO> basePageByApplicationId(Long appServiceId, Pageable pageable, Date startTime, Date endTime);

    void baseDeleteWithoutCommit();

    List<DevopsGitlabPipelineDTO> baseListByAppIdAndBranch(Long appServiceId, String branch);
}
