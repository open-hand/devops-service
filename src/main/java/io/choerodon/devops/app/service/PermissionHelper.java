package io.choerodon.devops.app.service;

import javax.annotation.Nullable;

import io.choerodon.devops.infra.dto.UserAttrDTO;

/**
 * 校验权限相关
 *
 * @author zmf
 * @since 19-12-27
 */
public interface PermissionHelper {
    /**
     * 判断指定用户是否是root
     *
     * @param userId 用户id
     * @return true表示是
     */
    boolean isRoot(Long userId);

    /**
     * 判断当前用户是否是root
     *
     * @return true表示是
     */
    boolean isRoot();

    /**
     * 首先判断当前用户是否同步成功，如果没有同步成功，返回false
     * 其次判断当前用户是否是项目所有者或者root用户
     *
     * @param projectId 项目id
     * @return true表示是
     */
    boolean isGitlabProjectOwnerOrRoot(Long projectId);

    /**
     * 首先判断用户是否同步成功，如果没有同步成功，返回false
     * 其次判断指定用户是否是项目所有者或者root用户
     *
     * @param projectId 项目id
     * @param iamUserId iamUserId
     * @return true表示是
     */
    boolean isGitlabProjectOwnerOrRoot(Long projectId, Long iamUserId);

    /**
     * 通过已经查询的用户纪录和IamProjectId判断用户是否是项目所有者或者owner
     *
     * @param projectId   项目id
     * @param userAttrDTO 用户纪录
     * @return true表示是
     */
    boolean isGitlabProjectOwnerOrRoot(Long projectId, @Nullable UserAttrDTO userAttrDTO);

    /**
     * 校验用户是否是项目所有者或者Root，如果不是，抛出异常
     *
     * @param projectId 项目id
     * @param iamUserId 用户id
     */
    void checkProjectOwnerOrRoot(Long projectId, Long iamUserId);

    /**
     * 通过已经查询的用户纪录和IamProjectId校验用户是否是项目所有者或者Root，如果不是，抛出异常
     *
     * @param projectId   项目id
     * @param userAttrDTO 用户纪录
     */
    void checkProjectOwnerOrRoot(Long projectId, @Nullable UserAttrDTO userAttrDTO);
}
