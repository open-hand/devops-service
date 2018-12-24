package io.choerodon.devops.app.service;

import java.util.Date;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.*;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

public interface DevopsGitlabPipelineService {

    void create(PipelineWebHookDTO pipelineWebHookDTO, String token);

    void updateStages(JobWebHookDTO jobWebHookDTO);

    PipelineTimeDTO getPipelineTime(Long appId, Date startTime, Date endTime);

    PipelineFrequencyDTO getPipelineFrequency(Long appId, Date startTime, Date endTime);

    void handleCreate(PipelineWebHookDTO pipelineWebHookDTO);

    Page<DevopsGitlabPipelineDTO> pagePipelines(Long appId, String branch, PageRequest pageRequest, Date startTime, Date endTime);
}
