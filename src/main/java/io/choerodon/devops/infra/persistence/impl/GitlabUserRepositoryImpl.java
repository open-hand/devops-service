package io.choerodon.devops.infra.persistence.impl;

import feign.FeignException;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabUserE;
import io.choerodon.devops.domain.application.event.GitlabUserEvent;
import io.choerodon.devops.domain.application.repository.GitlabUserRepository;
import io.choerodon.devops.infra.dataobject.gitlab.UserDO;
import io.choerodon.devops.infra.feign.GitlabServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Created by Zenger on 2018/3/28.
 */
@Component
public class GitlabUserRepositoryImpl implements GitlabUserRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitlabUserRepositoryImpl.class);

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
            throw new CommonException(e);
        }
        return ConvertHelper.convert(responseEntity.getBody(), GitlabUserE.class);
    }

    @Override
    public GitlabUserE getUserByUserName(String userName) {
        ResponseEntity<UserDO> responseEntity;
        try {
            responseEntity = gitlabServiceClient.queryUserByUserName(userName);
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
            throw new CommonException(e);
        }
        return ConvertHelper.convert(responseEntity.getBody(), GitlabUserE.class);
    }

    @Override
    public void isEnabledGitlabUser(Integer userId) {

        try {
            gitlabServiceClient.enabledUserByUserId(userId);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    @Override
    public void disEnabledGitlabUser(Integer userId) {
        try {
            gitlabServiceClient.disEnabledUserByUserId(userId);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    @Override
    public GitlabUserE getGitlabUserByUserId(Integer userId) {
        ResponseEntity<UserDO> responseEntity;
        try {
            responseEntity = gitlabServiceClient.queryUserByUserId(userId);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
        return ConvertHelper.convert(responseEntity.getBody(), GitlabUserE.class);
    }

    @Override
    public void initMockService(GitlabServiceClient gitlabServiceClient) {
        this.gitlabServiceClient = gitlabServiceClient;
    }

    @Override
    public Boolean checkEmailIsExist(String email) {
        return gitlabServiceClient.checkEmailIsExist(email).getBody();
    }
}
