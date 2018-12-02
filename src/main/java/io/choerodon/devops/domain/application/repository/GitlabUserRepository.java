package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.domain.application.entity.gitlab.GitlabUserE;
import io.choerodon.devops.domain.application.event.GitlabUserEvent;
import io.choerodon.devops.infra.feign.GitlabServiceClient;

/**
 * Created by Zenger on 2018/3/28.
 */
public interface GitlabUserRepository {

    GitlabUserE createGitLabUser(String password, Integer projectsLimit, GitlabUserEvent gitlabUserEvent);

    GitlabUserE updateGitLabUser(Integer userId, Integer projectsLimit, GitlabUserEvent gitlabUserEvent);

    void isEnabledGitlabUser(Integer userId);

    void disEnabledGitlabUser(Integer userId);

    GitlabUserE getGitlabUserByUserId(Integer userId);

    void initMockService(GitlabServiceClient gitlabServiceClient);
}
