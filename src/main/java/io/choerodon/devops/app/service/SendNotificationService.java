package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import io.choerodon.core.notify.NoticeSendDTO;
import io.choerodon.devops.api.vo.DevopsUserPermissionVO;
import io.choerodon.devops.infra.dto.AppServiceDTO;

/**
 * 发送通知的服务
 *
 * @author zmf
 * @since 12/5/19
 */
public interface SendNotificationService {

    /**
     * 发送通知
     *
     * @param sendSettingCode 通知code
     * @param sourceId        projectId, organizationId, 0L
     * @param targetUsers     目标用户
     * @param params          参数映射
     */
    void sendNotices(String sendSettingCode, Long sourceId, List<NoticeSendDTO.User> targetUsers, Map<String, Object> params);

    /**
     * 当应用服务创建失败后，发送消息
     *
     * @param appServiceId 应用服务id
     */
    void sendWhenAppServiceFailure(Long appServiceId);

    /**
     * 当应用服务启用后，发送消息
     *
     * @param appServiceId 应用服务id
     */
    void sendWhenAppServiceEnabled(Long appServiceId);

    /**
     * 当应用服务停用后，发送消息
     *
     * @param appServiceId 应用服务id
     */
    void sendWhenAppServiceDisabled(Long appServiceId);

    /**
     * 删除应用服务通知
     */
    void sendWhenAppServiceDelete(List<DevopsUserPermissionVO> devopsUserPermissionVOS, AppServiceDTO appServiceDTO);


    /**
     * 当持续集成失败后
     *
     * @param gitlabPipelineId         gitlab的pipeline的id(GitLab存的id)
     * @param appServiceDTO            持续集成流水线所属的应用服务
     * @param pipelineOperatorUserName pipeline操作者的用户名(正常情况下，pipeline中包含的用户名是猪齿鱼中用户的登录名)
     */
    void sendWhenCDFailure(Long gitlabPipelineId, AppServiceDTO appServiceDTO, String pipelineOperatorUserName);


    /**
     * 当有合并请求需要审核时
     *
     * @param gitlabProjectId gitlab项目id
     * @param mergeRequestId  合并请求的id
     */
    void sendWhenMergeRequestAuditEvent(Integer gitlabProjectId, Long mergeRequestId);


    /**
     * 当合并请求被关闭时
     *
     * @param gitlabProjectId gitlab项目id
     * @param mergeRequestId  合并请求的id
     * @param userLoginName   iam_user login_name
     */
    void sendWhenMergeRequestClosed(Integer gitlabProjectId, Long mergeRequestId, String userLoginName);


    /**
     * 当合并请求被通过时
     *
     * @param gitlabProjectId gitlab项目id
     * @param mergeRequestId  合并请求的id
     * @param userLoginName   iam_user login_name
     */
    void sendWhenMergeRequestPassed(Integer gitlabProjectId, Long mergeRequestId, String userLoginName);

    /**
     * 当创建实例失败后
     *
     * @param envId             环境id
     * @param resourceName      资源的名称
     * @param creatorId         资源创建者的id
     * @param resourceCommandId 资源的command id (不为null时校验command的commandType是不是create)
     */
    void sendWhenInstanceCreationFailure(Long envId, String resourceName, Long creatorId, @Nullable Long resourceCommandId);

    /**
     * 当创建网络失败后
     *
     * @param envId             环境id
     * @param resourceName      资源的名称
     * @param creatorId         资源创建者的id
     * @param resourceCommandId 资源的command id (不为null时校验command的commandType是不是create)
     */
    void sendWhenServiceCreationFailure(Long envId, String resourceName, Long creatorId, @Nullable Long resourceCommandId);

    /**
     * 当创建域名失败后
     *
     * @param envId             环境id
     * @param resourceName      资源的名称
     * @param creatorId         资源创建者的id
     * @param resourceCommandId 资源的command id (不为null时校验command的commandType是不是create)
     */
    void sendWhenIngressCreationFailure(Long envId, String resourceName, Long creatorId, @Nullable Long resourceCommandId);

    /**
     * 当创建证书失败后
     *
     * @param envId             环境id
     * @param resourceName      资源的名称
     * @param creatorId         资源创建者的id
     * @param resourceCommandId 资源的command id (不为null时校验command的commandType是不是create)
     */
    void sendWhenCertificationCreationFailure(Long envId, String resourceName, Long creatorId, @Nullable Long resourceCommandId);

    /**
     * 当创建用户时，将用户的默认随机密码发送给用户
     *
     * @param userId   猪齿鱼用户id
     * @param password 密码
     */
    void sendForUserDefaultPassword(String userId, String password);
}
