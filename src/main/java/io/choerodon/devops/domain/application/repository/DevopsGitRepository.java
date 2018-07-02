package io.choerodon.devops.domain.application.repository;


/**
 * Creator: Runge
 * Date: 2018/7/2
 * Time: 14:03
 * Description:
 */
public interface DevopsGitRepository {
    void createTag(Integer gitLabProjectId, String tag, String ref, Integer userId);

    Integer getGitLabId(Long applicationId);

    Integer getGitlabUserId();
}
