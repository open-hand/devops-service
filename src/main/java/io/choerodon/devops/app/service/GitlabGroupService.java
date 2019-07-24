package io.choerodon.devops.app.service;

import io.choerodon.devops.app.eventhandler.payload.GitlabGroupPayload;

/**
 * Created with IntelliJ IDEA.
 * User: Runge
 * Date: 2018/4/8
 * Time: 10:04
 * Description:
 */
public interface GitlabGroupService {
    void createGroup(GitlabGroupPayload gitlabGroupPayload, String groupCodeSuffix);

    void updateGroup(GitlabGroupPayload gitlabGroupPayload, String groupCodeSuffix);
}
