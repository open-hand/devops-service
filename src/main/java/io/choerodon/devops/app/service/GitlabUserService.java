package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.GitlabUserRequestDTO;
import io.choerodon.devops.infra.dto.gitlab.GitLabUserDTO;

/**
 * Created by Zenger on 2018/3/28.
 */
public interface GitlabUserService {

    void createGitlabUser(GitlabUserRequestDTO gitlabUserReqDTO);

    void updateGitlabUser(GitlabUserRequestDTO gitlabUserReqDTO);

    void isEnabledGitlabUser(Integer userId);

    void disEnabledGitlabUser(Integer userId);

    Boolean checkEmailIsExist(String email);

    GitLabUserDTO getGitlabUserByUserId(Integer userId);
}
