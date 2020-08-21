package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.enums.AppServiceEvent;

/**
 * @author scp
 * @date 2020/6/11
 * @description
 */
public interface CheckGitlabAccessLevelService {

    /**
     * 校验用户gitlab权限
     *
     * @param projectId
     * @param appServiceId
     */
    void checkGitlabPermission(Long projectId, Long appServiceId, AppServiceEvent appServiceEvent);
}
