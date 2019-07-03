package io.choerodon.devops.infra.persistence.impl;

import io.choerodon.core.convertor.ConvertHelper;
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

        ResponseEntity<UserDO> responseEntity = gitlabServiceClient.createGitLabUser(
                    password, projectsLimit, gitlabUserEvent);
        return ConvertHelper.convert(responseEntity.getBody(), GitlabUserE.class);
    }

    @Override
    public GitlabUserE getUserByUserName(String userName) {
        ResponseEntity<UserDO> responseEntity = gitlabServiceClient.queryUserByUserName(userName);
        if (responseEntity.getStatusCodeValue() == 500) {
            return null;
        }
        return ConvertHelper.convert(responseEntity.getBody(), GitlabUserE.class);
    }

    @Override
    public GitlabUserE updateGitLabUser(Integer userId, Integer projectsLimit, GitlabUserEvent gitlabUserEvent) {
        ResponseEntity<UserDO> responseEntity;
            responseEntity = gitlabServiceClient.updateGitLabUser(
                    userId, projectsLimit, gitlabUserEvent);
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
        ResponseEntity<UserDO> responseEntity = gitlabServiceClient.queryUserByUserId(userId);
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
