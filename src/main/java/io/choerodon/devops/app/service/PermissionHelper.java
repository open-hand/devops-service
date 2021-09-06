package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.DevopsServiceReqVO;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.UserAttrDTO;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 校验权限相关
 *
 * @author zmf
 * @since 19-12-27
 */
public interface PermissionHelper {
    /**
     * 判断指定用户是否是Gitlab的admin
     *
     * @param userId 用户id
     * @return true表示是
     */
    boolean isGitlabAdmin(Long userId);

    /**
     * 判断指定用户是否是Gitlab的admin
     *
     * @return true表示是
     */
    boolean isGitlabAdmin();

    /**
     * 首先判断当前用户是否同步成功，如果没有同步成功，返回false
     * 其次判断当前用户是否是项目所有者或者gitlab admin用户
     *
     * @param projectId 项目id
     * @return true表示是
     */
    boolean isGitlabProjectOwnerOrGitlabAdmin(Long projectId);

    /**
     * 首先判断用户是否同步成功，如果没有同步成功，返回false
     * 其次判断指定用户是否是项目所有者或者gitlab admin用户（包括组织层的root）
     *
     * @param projectId 项目id
     * @param iamUserId iamUserId
     * @return true表示是
     */
    boolean isGitlabProjectOwnerOrGitlabAdmin(Long projectId, Long iamUserId);

    /**
     * 通过已经查询的用户纪录和IamProjectId判断用户是否是项目所有者或者owner
     *
     * @param projectId   项目id
     * @param userAttrDTO 用户纪录
     * @return true表示是
     */
    boolean isGitlabProjectOwnerOrGitlabAdmin(Long projectId, @Nullable UserAttrDTO userAttrDTO);

    /**
     * 校验用户是否是项目所有者或者gitlab admin用户，如果不是，抛出异常
     *
     * @param projectId 项目id
     * @param iamUserId 用户id
     */
    void checkProjectOwnerOrGitlabAdmin(Long projectId, Long iamUserId);

    /**
     * 通过已经查询的用户纪录和IamProjectId校验用户是否是项目所有者或者gitlab admin用户，如果不是，抛出异常
     *
     * @param projectId   项目id
     * @param userAttrDTO 用户纪录
     */
    void checkProjectOwnerOrGitlabAdmin(Long projectId, @Nullable UserAttrDTO userAttrDTO);

    /**
     * 判断指定用户是否是root用户
     *
     * @param userId
     * @return
     */
    Boolean isRoot(Long userId);

    /**
     * 判断指定用户是否是组织Root用户
     *
     * @param userId         用户id
     * @param organizationId 组织id
     * @return true表示是
     */
    Boolean isOrganizationRoot(Long userId, Long organizationId);

    /**
     * 判断指定用户是否是项目所有者
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @return 是否是项目所有者
     */
    Boolean isProjectOwner(Long userId, Long projectId);

    /**
     * 判断指定用户是否是gitlab owner
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @return 是否是gitlab owner
     */
    Boolean isGitlabProjectOwner(Long userId, Long projectId);

    /**
     * 这个项目是否有集群的权限
     *
     * @param clusterId 集群id
     * @param projectId 项目id
     * @return true表示有权限
     */
    boolean projectPermittedToCluster(Long clusterId, Long projectId);

    /**
     * 项目是否有证书的权限
     *
     * @param certId    证书id
     * @param projectId 项目id
     * @return true表示有
     */
    boolean projectPermittedToCert(Long certId, Long projectId);

    /**
     * 检查环境是否属于指定项目
     *
     * @param projectId 项目id
     * @param envId     环境id
     */
    DevopsEnvironmentDTO checkEnvBelongToProject(Long projectId, Long envId);

    /**
     * 检查应用是否属于指定项目
     *
     * @param projectId    项目id
     * @param appServiceId 应用id
     * @return 校验结果
     */
    AppServiceDTO checkAppServiceBelongToProject(Long projectId, Long appServiceId);

    /**
     * 检查多个应用服务是否属于指定项目
     * @param projectId
     * @param appServiceIds
     */
    void checkAppServicesBelongToProject(Long projectId, List<Long> appServiceIds);

    /**
     * 校验部署方式是否唯一
     * @param devopsServiceReqVO
     */
    void checkDeploymentWay(DevopsServiceReqVO devopsServiceReqVO);
}
