package io.choerodon.devops.infra.persistence.impl;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabUserE;
import io.choerodon.devops.domain.application.event.GitlabUserEvent;
import io.choerodon.devops.domain.application.repository.GitlabUserRepository;
import io.choerodon.devops.infra.dataobject.gitlab.UserDO;
import io.choerodon.devops.infra.feign.GitlabServiceClient;

/**
 * Created by Zenger on 2018/3/28.
 */
@Component
public class GitlabUserRepositoryImpl implements GitlabUserRepository {

    private GitlabServiceClient gitlabServiceClient;

    public GitlabUserRepositoryImpl(GitlabServiceClient gitlabServiceClient) {
        this.gitlabServiceClient = gitlabServiceClient;
    }

    @Override
    public GitlabUserE getGitlabUserByUsername(String userName) {
        ResponseEntity<UserDO> responseEntity = gitlabServiceClient.queryUserByUsername(userName);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            return null;
        }
        return ConvertHelper.convert(responseEntity.getBody(), GitlabUserE.class);
    }

    @Override
    public GitlabUserE createGitLabUser(String password, Integer projectsLimit, GitlabUserEvent gitlabUserEvent) {
        ResponseEntity<UserDO> responseEntity = gitlabServiceClient.createGitLabUser(
                password, projectsLimit, gitlabUserEvent);
        if (responseEntity.getStatusCode() != HttpStatus.CREATED) {
            return null;
        }
        return ConvertHelper.convert(responseEntity.getBody(), GitlabUserE.class);
    }

    @Override
    public GitlabUserE updateGitLabUser(String username, Integer projectsLimit, GitlabUserEvent gitlabUserEvent) {
        ResponseEntity<UserDO> responseEntity = gitlabServiceClient.updateGitLabUser(
                username, projectsLimit, gitlabUserEvent);
        if (responseEntity.getStatusCode() != HttpStatus.CREATED) {
            return null;
        }
        return ConvertHelper.convert(responseEntity.getBody(), GitlabUserE.class);
    }

    @Override
    public void isEnabledGitlabUser(String userName) {
        gitlabServiceClient.enabledUserByUsername(userName);
    }

    @Override
    public void disEnabledGitlabUser(String userName) {
        gitlabServiceClient.disEnabledUserByUsername(userName);
    }

    @Override
    public GitlabUserE getGitlabUserByUserId(Integer userId) {
        ResponseEntity<UserDO> responseEntity = gitlabServiceClient.queryUserByUserId(userId);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            return null;
        }
        return ConvertHelper.convert(responseEntity.getBody(), GitlabUserE.class);
    }
}
