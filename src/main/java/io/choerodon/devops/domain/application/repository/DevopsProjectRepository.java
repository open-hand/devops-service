package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.domain.application.entity.DevopsProjectE;
import io.choerodon.devops.infra.dataobject.DevopsProjectDTO;

/**
 * Created by younger on 2018/3/29.
 */
public interface DevopsProjectRepository {
    void createProject(DevopsProjectDTO devopsProjectDO);

    void updateProjectAttr(DevopsProjectDTO devopsProjectDO);

    DevopsProjectE queryDevopsProject(Long projectId);

    DevopsProjectE queryByGitlabGroupId(Integer gitlabGroupId);

    DevopsProjectE queryByEnvGroupId(Integer envGroupId);

}
