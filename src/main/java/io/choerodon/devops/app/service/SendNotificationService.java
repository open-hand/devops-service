package io.choerodon.devops.app.service;

/**
 * 发送通知的服务
 *
 * @author zmf
 * @since 12/5/19
 */
public interface SendNotificationService {
    /**
     * 当应用服务创建失败后，发送消息
     *
     * @param organizationId  组织id
     * @param projectId       项目id
     * @param projectName     项目名称
     * @param projectCategory 项目类别
     * @param appServiceName  应用服务名称
     */
    void sendWhenAppServiceFailure(Long organizationId,
                                   Long projectId,
                                   String projectName,
                                   String projectCategory,
                                   String appServiceName);

    /**
     * 当应用服务启用后，发送消息
     *
     * @param organizationId  组织id
     * @param projectId       项目id
     * @param projectName     项目名称
     * @param projectCategory 项目类别
     * @param appServiceName  应用服务名称
     */
    void sendWhenAppServiceEnabled(Long organizationId,
                                   Long projectId,
                                   String projectName,
                                   String projectCategory,
                                   String appServiceName);

    /**
     * 当应用服务停用后，发送消息
     *
     * @param organizationId  组织id
     * @param projectId       项目id
     * @param projectName     项目名称
     * @param projectCategory 项目类别
     * @param appServiceName  应用服务名称
     */
    void sendWhenAppServiceDisabled(Long organizationId,
                                    Long projectId,
                                    String projectName,
                                    String projectCategory,
                                    String appServiceName);

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
     * @param gitlabUrl        gitlab的http(s)地址
     * @param organizationCode 组织code
     * @param projectCode      项目code
     * @param projectName      项目名称
     * @param appServiceCode   应用服务code
     * @param appServiceName   应用服务名称
     * @param realName         合并请求提交者的名称
     * @param mergeRequestId   合并请求的id
     */
    void sendWhenMergeRequestAuditEvent(String gitlabUrl,
                                        String organizationCode,
                                        String projectCode,
                                        String projectName,
                                        String appServiceCode,
                                        String appServiceName,
                                        String realName,
                                        Long mergeRequestId);

    /**
     * 当合并请求被关闭时
     *
     * @param gitlabUrl        gitlab的http(s)地址
     * @param organizationCode 组织code
     * @param projectCode      项目code
     * @param projectName      项目名称
     * @param appServiceCode   应用服务code
     * @param appServiceName   应用服务名称
     * @param realName         关闭合并请求的操作者的名称
     * @param mergeRequestId   合并请求的id
     */
    void sendWhenMergeRequestClosed(String gitlabUrl,
                                    String organizationCode,
                                    String projectCode,
                                    String projectName,
                                    String appServiceCode,
                                    String appServiceName,
                                    String realName,
                                    Long mergeRequestId);

    /**
     * 当合并请求被通过时
     *
     * @param gitlabUrl        gitlab的http(s)地址
     * @param organizationCode 组织code
     * @param projectCode      项目code
     * @param projectName      项目名称
     * @param appServiceCode   应用服务code
     * @param appServiceName   应用服务名称
     * @param realName         通过合并请求的操作者的名称
     * @param mergeRequestId   合并请求的id
     */
    void sendWhenMergeRequestPassed(String gitlabUrl,
                                    String organizationCode,
                                    String projectCode,
                                    String projectName,
                                    String appServiceCode,
                                    String appServiceName,
                                    String realName,
                                    Long mergeRequestId);

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
