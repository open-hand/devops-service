package io.choerodon.devops.app.service;

import java.util.Date;

import io.choerodon.devops.api.dto.JobWebHookDTO;
import io.choerodon.devops.api.dto.PipelineFrequencyDTO;
import io.choerodon.devops.api.dto.PipelineTimeDTO;
import io.choerodon.devops.api.dto.PipelineWebHookDTO;

public interface DevopsGitlabPipelineService {

    void create(PipelineWebHookDTO pipelineWebHookDTO, String token);

    void updateStages(JobWebHookDTO jobWebHookDTO);

    PipelineTimeDTO getPipelineTime(Long appId, Date startTime, Date endTime);

    PipelineFrequencyDTO getPipelineFrequency(Long appId, Date startTime, Date endTime);

    void handleCreate(PipelineWebHookDTO pipelineWebHookDTO);
}
