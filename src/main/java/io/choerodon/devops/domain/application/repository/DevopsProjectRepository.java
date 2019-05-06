package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.domain.application.entity.DevopsProjectE;
import io.choerodon.devops.infra.dataobject.DevopsProjectDO;

/**
 * Created by younger on 2018/3/29.
 */
public interface DevopsProjectRepository {
    void createProject(DevopsProjectDO devopsProjectDO);

    void updateProjectAttr(DevopsProjectDO devopsProjectDO);

    DevopsProjectE queryDevopsProject(Long projectId);

    DevopsProjectE queryByGitlabGroupId(Integer gitlabGroupId);

    DevopsProjectE queryByEnvGroupId(Integer envGroupId);

}
