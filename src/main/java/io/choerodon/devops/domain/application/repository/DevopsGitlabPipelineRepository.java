package io.choerodon.devops.domain.application.repository;

import java.util.Date;
import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.iam.entity.DevopsGitlabPipelineE;
import io.choerodon.devops.infra.dto.DevopsGitlabPipelineDO;



public interface DevopsGitlabPipelineRepository {

    void create(DevopsGitlabPipelineE devopsGitlabPipelineE);

    DevopsGitlabPipelineE queryByGitlabPipelineId(Long id);

    void update(DevopsGitlabPipelineE devopsGitlabPipelineE);

    DevopsGitlabPipelineE queryByCommitId(Long commitId);

    List<DevopsGitlabPipelineDO> listPipeline(Long appId, Date startTime, Date endTime);

    PageInfo<DevopsGitlabPipelineDO> pagePipeline(Long appId, PageRequest pageRequest, Date startTime, Date endTime);

    void deleteWithoutCommit();

    List<DevopsGitlabPipelineDO> listByBranch(Long appId, String branch);

}
