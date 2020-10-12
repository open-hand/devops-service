package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.GitlabUserRequestVO;

/**
 * Created by Zenger on 2018/3/28.
 */
public interface GitlabUserService {
    /**
     * 重置用户的gitlab密码
     *
     * @param userId 猪齿鱼用户id
     * @return 重置后的密码
     */
    String resetGitlabPassword(Long userId);

    void createGitlabUser(GitlabUserRequestVO gitlabUserReqDTO);

    void updateGitlabUser(GitlabUserRequestVO gitlabUserReqDTO);

    void isEnabledGitlabUser(Long userId);

    void disEnabledGitlabUser(Long userId);

    Boolean doesEmailExists(String email);

    void assignAdmins(List<Long> iamUserIds);

    void deleteAdmin(Long iamUserId);
}
