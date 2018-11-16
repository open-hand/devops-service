package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupE;
import io.choerodon.devops.infra.dataobject.DevopsProjectDO;

/**
 * Created by younger on 2018/3/29.
 */
public interface DevopsProjectRepository {
    void createProject(DevopsProjectDO devopsProjectDO);

    void updateProjectAttr(DevopsProjectDO devopsProjectDO);

    GitlabGroupE queryDevopsProject(Long projectId);

    GitlabGroupE queryByGitlabGroupId(Integer gitlabGroupId);

    GitlabGroupE queryByEnvGroupId(Integer envGroupId);

}
