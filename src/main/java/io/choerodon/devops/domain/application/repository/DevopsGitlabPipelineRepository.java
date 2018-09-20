package io.choerodon.devops.domain.application.repository;

import java.util.Date;
import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.domain.application.entity.DevopsGitlabPipelineE;
import io.choerodon.devops.infra.dataobject.DevopsGitlabPipelineDO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

public interface DevopsGitlabPipelineRepository {

    void create(DevopsGitlabPipelineE devopsGitlabPipelineE);

    DevopsGitlabPipelineE queryByGitlabPipelineId(Long id);

    void update(DevopsGitlabPipelineE devopsGitlabPipelineE);

    DevopsGitlabPipelineE queryByCommitId(Long commitId);

    List<DevopsGitlabPipelineDO> listPipeline(Long appId, Date startTime, Date endTime);

    Page<DevopsGitlabPipelineDO> pagePipeline(Long appId, PageRequest pageRequest, Date startTime, Date endTime);

}
