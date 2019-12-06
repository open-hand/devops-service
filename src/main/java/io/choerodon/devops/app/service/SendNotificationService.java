package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Map;

import io.choerodon.core.notify.NoticeSendDTO;
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
     */
    void sendWhenMergeRequestClosed(Integer gitlabProjectId, Long mergeRequestId);


    /**
     * 当合并请求被通过时
     *
     * @param gitlabProjectId gitlab项目id
     * @param mergeRequestId  合并请求的id
     */
    void sendWhenMergeRequestPassed(Integer gitlabProjectId, Long mergeRequestId);

    /**
     * 当创建实例失败后
     *
     * @param envId        环境id
     * @param resourceName 资源的名称
     * @param creatorId    资源创建者的id
     */
    void sendWhenInstanceCreationFailure(Long envId, String resourceName, Long creatorId);

    /**
     * 当创建网络失败后
     *
     * @param envId        环境id
     * @param resourceName 资源的名称
     * @param creatorId    资源创建者的id
     */
    void sendWhenServiceCreationFailure(Long envId, String resourceName, Long creatorId);

    /**
     * 当创建域名失败后
     *
     * @param envId        环境id
     * @param resourceName 资源的名称
     * @param creatorId    资源创建者的id
     */
    void sendWhenIngressCreationFailure(Long envId, String resourceName, Long creatorId);

    /**
     * 当创建证书失败后
     *
     * @param envId        环境id
     * @param resourceName 资源的名称
     * @param creatorId    资源创建者的id
     */
    void sendWhenCertificationCreationFailure(Long envId, String resourceName, Long creatorId);
}
