package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.domain.application.entity.gitlab.GitlabUserE;
import io.choerodon.devops.domain.application.event.GitlabUserEvent;

/**
 * Created by Zenger on 2018/3/28.
 */
public interface GitlabUserRepository {

    GitlabUserE getGitlabUserByUsername(String userName);

    GitlabUserE createGitLabUser(String password, Integer projectsLimit, GitlabUserEvent gitlabUserEvent);

    GitlabUserE updateGitLabUser(String username, Integer projectsLimit, GitlabUserEvent gitlabUserEvent);

    void isEnabledGitlabUser(String userName);

    void disEnabledGitlabUser(String userName);
}
