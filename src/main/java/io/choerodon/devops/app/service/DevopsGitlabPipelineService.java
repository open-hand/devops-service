package io.choerodon.devops.app.service;

import java.util.Date;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.*;

public interface DevopsGitlabPipelineService {

    void create(PipelineWebHookDTO pipelineWebHookDTO, String token);

    void updateStages(JobWebHookDTO jobWebHookDTO);

    PipelineTimeDTO getPipelineTime(Long appId, Date startTime, Date endTime);

    PipelineFrequencyDTO getPipelineFrequency(Long appId, Date startTime, Date endTime);

    void handleCreate(PipelineWebHookDTO pipelineWebHookDTO);

    PageInfo<DevopsGitlabPipelineDTO> pagePipelines(Long appId, String branch, PageRequest pageRequest, Date startTime, Date endTime);
}
