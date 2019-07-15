package io.choerodon.devops.domain.application.repository;

import java.util.Date;
import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.iam.entity.DevopsGitlabPipelineE;
<<<<<<< HEAD
<<<<<<< HEAD
import io.choerodon.devops.infra.dto.DevopsGitlabPipelineDO;


=======
import io.choerodon.devops.infra.dataobject.DevopsGitlabPipelineDO;
>>>>>>> [IMP] 修改AppControler重构
=======
import io.choerodon.devops.infra.dto.DevopsGitlabPipelineDTO;
>>>>>>> [IMP] 重构Repository

public interface DevopsGitlabPipelineRepository {

    void baseCreate(DevopsGitlabPipelineE devopsGitlabPipelineE);

    DevopsGitlabPipelineE baseQueryByGitlabPipelineId(Long id);

    void baseUpdate(DevopsGitlabPipelineE devopsGitlabPipelineE);

    DevopsGitlabPipelineE baseQueryByCommitId(Long commitId);

    List<DevopsGitlabPipelineDTO> baseListByApplicationId(Long appId, Date startTime, Date endTime);

    PageInfo<DevopsGitlabPipelineDTO> basePageByApplicationId(Long appId, PageRequest pageRequest, Date startTime, Date endTime);

    void baseDeleteWithoutCommit();

    List<DevopsGitlabPipelineDTO> baseListByAppIdAndBranch(Long appId, String branch);

}
