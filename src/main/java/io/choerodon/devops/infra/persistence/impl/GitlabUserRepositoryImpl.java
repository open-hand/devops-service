package io.choerodon.devops.infra.persistence.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import feign.FeignException;
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
    public GitlabUserE createGitLabUser(String password, Integer projectsLimit, GitlabUserEvent gitlabUserEvent) {
        ResponseEntity<UserDO> responseEntity;
        try {
            responseEntity = gitlabServiceClient.createGitLabUser(
                    password, projectsLimit, gitlabUserEvent);
        } catch (FeignException e) {
            return null;
        }
        return ConvertHelper.convert(responseEntity.getBody(), GitlabUserE.class);
    }

    @Override
    public GitlabUserE updateGitLabUser(Integer userId, Integer projectsLimit, GitlabUserEvent gitlabUserEvent) {
        ResponseEntity<UserDO> responseEntity;
        try {
            responseEntity = gitlabServiceClient.updateGitLabUser(
                    userId, projectsLimit, gitlabUserEvent);
        } catch (FeignException e) {
            return null;
        }
        return ConvertHelper.convert(responseEntity.getBody(), GitlabUserE.class);
    }

    @Override
    public void isEnabledGitlabUser(Integer userId) {
        gitlabServiceClient.enabledUserByUserId(userId);
    }

    @Override
    public void disEnabledGitlabUser(Integer userId) {
        gitlabServiceClient.disEnabledUserByUserId(userId);
    }

    @Override
    public GitlabUserE getGitlabUserByUserId(Integer userId) {
        ResponseEntity<UserDO> responseEntity;
        try {
            responseEntity = gitlabServiceClient.queryUserByUserId(userId);
        } catch (FeignException e) {
            return null;
        }
        return ConvertHelper.convert(responseEntity.getBody(), GitlabUserE.class);
    }
}
