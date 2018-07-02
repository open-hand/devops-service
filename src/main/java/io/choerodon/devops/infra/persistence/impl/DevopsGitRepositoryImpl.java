package io.choerodon.devops.infra.persistence.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.devops.domain.application.repository.DevopsGitRepository;
import io.choerodon.devops.infra.feign.GitlabServiceClient;

/**
 * Creator: Runge
 * Date: 2018/7/2
 * Time: 14:02
 * Description:
 */
@Component
public class DevopsGitRepositoryImpl implements DevopsGitRepository {
    @Autowired
    private GitlabServiceClient gitlabServiceClient;

    @Override
    public void createTag(Integer projectId, String tag, String ref, Integer userId) {
        gitlabServiceClient.createTag(projectId, tag, ref, userId);
    }
}
