package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.DevopsProjectDTO;

/**
 * Created by Sheep on 2019/7/15.
 */
public interface DevopsProjectService {
    DevopsProjectDTO baseQueryByGitlabAppGroupId(Integer appGroupId);

    DevopsProjectDTO baseQueryByProjectId(Long projectId);
}
