package io.choerodon.devops.app.service;

import io.choerodon.devops.app.eventhandler.payload.GitlabGroupPayload;
import io.choerodon.devops.infra.dto.gitlab.GroupDTO;

/**
 * Created with IntelliJ IDEA.
 * User: Runge
 * Date: 2018/4/8
 * Time: 10:04
 * Description:
 */
public interface GitlabGroupService {
    void createGroup(GitlabGroupPayload gitlabGroupPayload, String groupCodeSuffix);

    /**
     * 为应用下载创建 group
     * @param gitlabGroupPayload
     * @return
     */
    GroupDTO createAppMarketGroup(GitlabGroupPayload gitlabGroupPayload);

    void updateGroup(GitlabGroupPayload gitlabGroupPayload, String groupCodeSuffix);
}
