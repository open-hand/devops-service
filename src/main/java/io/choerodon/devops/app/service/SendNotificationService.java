package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Map;

import io.choerodon.core.notify.NoticeSendDTO;

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
     * @param gitlabUrl        gitlab的http(s)地址
     * @param organizationCode 组织code
     * @param projectCode      项目code
     * @param projectName      项目名称
     * @param appServiceCode   应用服务code
     * @param appServiceName   应用服务名称
     */
    void sendWhenCDFailure(String gitlabUrl,
                           String organizationCode,
                           String projectCode,
                           String projectName,
                           String appServiceCode,
                           String appServiceName);


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
     * @param projectName  项目名称
     * @param envName      环境名称
     * @param resourceName 资源的名称
     */
    void sendWhenInstanceCreationFailure(String projectName, String envName, String resourceName);

    /**
     * 当创建网络失败后
     *
     * @param projectName  项目名称
     * @param envName      环境名称
     * @param resourceName 资源的名称
     */
    void sendWhenServiceCreationFailure(String projectName, String envName, String resourceName);

    /**
     * 当创建域名失败后
     *
     * @param projectName  项目名称
     * @param envName      环境名称
     * @param resourceName 资源的名称
     */
    void sendWhenIngressCreationFailure(String projectName, String envName, String resourceName);

    /**
     * 当创建证书失败后
     *
     * @param projectName  项目名称
     * @param envName      环境名称
     * @param resourceName 资源的名称
     */
    void sendWhenCertificationCreationFailure(String projectName, String envName, String resourceName);
}
