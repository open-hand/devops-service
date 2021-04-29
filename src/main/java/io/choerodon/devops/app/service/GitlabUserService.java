package io.choerodon.devops.app.service;

import java.util.Iterator;
import java.util.List;

import io.choerodon.devops.api.vo.GitlabUserRequestVO;
import io.choerodon.devops.infra.dto.DevopsUserSyncRecordDTO;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.enums.UserSyncType;
import io.choerodon.devops.infra.util.UserSyncErrorBuilder;

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

    /**
     * 开启一个新的事务创建用户
     *
     * @param gitlabUserReqDTO 用户信息
     */
    void createGitlabUserInNewTx(GitlabUserRequestVO gitlabUserReqDTO);

    void createGitlabUser(GitlabUserRequestVO gitlabUserReqDTO);

    void updateGitlabUser(GitlabUserRequestVO gitlabUserReqDTO);

    /**
     * 如果后台没有同步用户任务，触发异步同步用户任务
     *
     * @param userSyncType 触发同步的来源类型
     */
    void asyncHandleAllUsers(UserSyncType userSyncType);

    /**
     * 同步所有用户
     *
     * @param userSyncType      触发同步的来源类型
     * @param userSyncRecordDTO 用户同步记录
     */
    void syncAllUsers(UserSyncType userSyncType, DevopsUserSyncRecordDTO userSyncRecordDTO);

    /**
     * 分批同步用户
     *
     * @param iamUserIds              总的用户id的迭代器
     * @param batchSize               一批该处理的数量，例如1000
     * @param processedSize           已经处理的用户数量
     * @param totalSize               所有的用户总数
     * @param devopsUserSyncRecordDTO 同步记录
     * @param userSyncErrorBuilder    同步用户的错误信息
     */
    void batchSyncUsersInNewTx(Iterator<Long> iamUserIds, int batchSize, int processedSize, int totalSize, DevopsUserSyncRecordDTO devopsUserSyncRecordDTO, UserSyncErrorBuilder userSyncErrorBuilder);

    void isEnabledGitlabUser(Long userId);

    void disEnabledGitlabUser(Long userId);

    void disEnabledGitlabUser(UserAttrDTO userAttrDTO);

    Boolean doesEmailExists(String email);

    void assignAdmins(List<Long> iamUserIds);

    void assignAdmin(UserAttrDTO user);

    void deleteAdmin(Long iamUserId);
}
