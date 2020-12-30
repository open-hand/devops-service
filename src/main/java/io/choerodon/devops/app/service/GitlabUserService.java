package io.choerodon.devops.app.service;

import java.util.Iterator;
import java.util.List;

import io.choerodon.devops.api.vo.GitlabUserRequestVO;
import io.choerodon.devops.infra.dto.UserAttrDTO;

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

    /**
     * 同步所有用户
     */
    void syncAllUsers();

    /**
     * 分批同步用户
     *
     * @param iamUserIds    总的用户id的迭代器
     * @param batchSize     一批该处理的数量，例如1000
     * @param processedSize 已经处理的用户数量
     * @param totalSize     所有的用户总数
     */
    void batchSyncUsersInNewTx(Iterator<Long> iamUserIds, int batchSize, int processedSize, int totalSize);

    void isEnabledGitlabUser(Long userId);

    void disEnabledGitlabUser(Long userId);

    void disEnabledGitlabUser(UserAttrDTO userAttrDTO);

    Boolean doesEmailExists(String email);

    void assignAdmins(List<Long> iamUserIds);

    void assignAdmin(UserAttrDTO user);

    void deleteAdmin(Long iamUserId);
}
