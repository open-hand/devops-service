package io.choerodon.devops.app.service;

/**
 * Creator: Runge
 * Date: 2018/7/2
 * Time: 14:39
 * Description:
 */
public interface DevopsGitService {
    void createTag(Long projectId, Long appId, String tag, String ref);
}
