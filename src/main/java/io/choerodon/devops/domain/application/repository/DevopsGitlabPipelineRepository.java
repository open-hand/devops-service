package io.choerodon.devops.domain.application.repository;

import java.util.Date;
import java.util.List;

import io.choerodon.devops.domain.application.entity.DevopsGitlabPipelineE;
import io.choerodon.devops.infra.dataobject.DevopsGitlabPipelineDO;

public interface DevopsGitlabPipelineRepository {

    void create(DevopsGitlabPipelineE devopsGitlabPipelineE);

    DevopsGitlabPipelineE queryByGitlabPipelineId(Long id);

    void update(DevopsGitlabPipelineE devopsGitlabPipelineE);

    DevopsGitlabPipelineE queryByCommitId(Long commitId);

    List<DevopsGitlabPipelineDO> pipelineTime(Long appId, Date startTime, Date endTime);
}
