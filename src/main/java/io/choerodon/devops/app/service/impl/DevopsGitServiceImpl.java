package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.devops.app.service.DevopsGitService;
import io.choerodon.devops.domain.application.repository.DevopsGitRepository;

/**
 * Creator: Runge
 * Date: 2018/7/2
 * Time: 14:44
 * Description:
 */
@Component
public class DevopsGitServiceImpl implements DevopsGitService {
    @Autowired
    private DevopsGitRepository devopsGitRepository;

    @Override
    public void createTag(Long projectId, Long appId, String tag, String ref) {
        Integer gitLabProjectId = 0;
        Integer gitLabUserId = 0;
        devopsGitRepository.createTag(gitLabProjectId, tag, ref, gitLabUserId);
    }
}
