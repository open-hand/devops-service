package io.choerodon.devops.app.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

import org.hzero.boot.message.entity.Receiver;

import io.choerodon.devops.app.eventhandler.payload.DevopsEnvUserPayload;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.gitlab.MemberDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;

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
     * @param receivers       目标用户
     * @param params          参数映射
     */
    void sendNotices(String sendSettingCode, List<Receiver> receivers, Map<String, String> params, Long projectId);

    /**
     * 创建应用服务发送webhook通知
     */
    void sendWhenAppServiceCreate(AppServiceDTO appServiceDTO);

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
    void sendWhenAppServiceDelete(List<MemberDTO> memberDTOS, AppServiceDTO appServiceDTO);


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
     * @param creatorId         资源创建者的id
     * @param resourceCommandId 资源的command id (不为null时校验command的commandType是不是create)
     */
    void sendWhenInstanceCreationFailure(AppServiceInstanceDTO appServiceInstanceDTO, Long creatorId, @Nullable Long resourceCommandId);

    /**
     * 当创建网络失败后
     *
     * @param creatorId         资源创建者的id
     * @param resourceCommandId 资源的command id (不为null时校验command的commandType是不是create)
     */
    void sendWhenServiceCreationFailure(DevopsServiceDTO devopsServiceDTO, Long creatorId, DevopsEnvironmentDTO devopsEnvironmentDTO, @Nullable Long resourceCommandId);

    /**
     * 当创建域名失败后
     *
     * @param creatorId         资源创建者的id
     * @param resourceCommandId 资源的command id (不为null时校验command的commandType是不是create)
     */
    void sendWhenIngressCreationFailure(DevopsIngressDTO devopsIngressDTO, Long creatorId, @Nullable Long resourceCommandId);

    /**
     * 当创建证书失败后
     *
     * @param creatorId         资源创建者的id
     * @param resourceCommandId 资源的command id (不为null时校验command的commandType是不是create)
     */
    void sendWhenCertificationCreationFailure(CertificationDTO certificationDTO, Long creatorId, @Nullable Long resourceCommandId);

    /**
     * 当创建用户时，将用户的默认随机密码发送给用户
     *
     * @param userId   猪齿鱼用户id
     * @param password 密码
     */
    void sendForUserDefaultPassword(String userId, String password);

    /**
     * 创建环境成功发送webhook
     */
    void sendWhenEnvCreate(DevopsEnvironmentDTO devopsEnvironmentDTO, Long organizationId);

    /**
     * 启用环境发送webhook
     */
    void sendWhenEnvEnable(DevopsEnvironmentDTO devopsEnvironmentDTO, Long organizationId);

    void sendWhenEnvDisable(DevopsEnvironmentDTO devopsEnvironmentDTO, Long organizationId);

    /**
     * 删除环境发送webhook
     */
    void sendWhenEnvDelete(DevopsEnvironmentDTO devopsEnvironmentDTO, Long organizationId);

    /**
     * 创建环境失败发送消息
     */
    void sendWhenCreateEnvFailed(DevopsEnvironmentDTO devopsEnvironmentDTO, Long organizationId);

    /**
     * 分配权限发送webhook json
     */
    void sendWhenEnvUpdatePermissions(DevopsEnvUserPayload devopsEnvUserPayload, ProjectDTO projectDTO);

    void sendWhenCreateCluster(DevopsClusterDTO devopsClusterDTO, ProjectDTO iamProject);

    /**
     * 激活集群发送webhook
     */
    void sendWhenActivateCluster(DevopsClusterDTO devopsClusterDTO);

    /**
     * 删除集群
     */
    void sendWhenDeleteCluster(DevopsClusterDTO devopsClusterDTO);

    /**
     * 组件安装失败发送webhook
     */
    void sendWhenResourceInstallFailed(DevopsClusterResourceDTO devopsClusterResourceDTO, String value, String type, Long clusterId, String payload);

    void sendWhenCDSuccess(AppServiceDTO appServiceDTO, String pipelineOperatorUserName);

    void sendWhenAppServiceVersion(AppServiceVersionDTO appServiceVersionDTO, AppServiceDTO appServiceDTO, ProjectDTO projectDTO);

    void sendWhenCreateClusterFail(DevopsClusterDTO devopsClusterDTO, ProjectDTO iamProject, String error);

    /**
     * 创建PVC资源成功或者失败webhook json 通知
     */
    void sendWhenPVCResource(DevopsPvcDTO devopsPvcDTO, DevopsEnvironmentDTO devopsEnvironmentDTO, String code);

    void sendWhenServiceCreationSuccessOrDelete(DevopsServiceDTO devopsServiceDTO, DevopsEnvironmentDTO devopsEnvironmentDTO, String code);

    /**
     * 实例创建成功 删除发送webhook json
     */
    void sendWhenInstanceSuccessOrDelete(AppServiceInstanceDTO appServiceInstanceDTO, String value);

    /**
     * 域名创建 删除 发送webhook 通知
     */
    void sendWhenIngressSuccessOrDelete(DevopsIngressDTO devopsIngressDTO, String code);

    void sendWhenCertSuccessOrDelete(CertificationDTO certificationDTO, String code);

    void sendWhenConfigMap(DevopsConfigMapDTO devopsConfigMapDTO, String value);

    void sendWhenSecret(DevopsSecretDTO devopsSecretDTO, String code);

    void sendPipelineAuditMassage(String type, List<Long> auditUser, Long pipelineRecordId, String stageName, Long stageId,Long detailsUserId);

    void sendCdPipelineNotice(Long pipelineRecordId, String type, Long userId, String email, HashMap<String, String> params);

    void sendCiPipelineNotice(Long pipelineRecordId, String type, Long userId, String email, HashMap<String, String> params);

    void sendCdPipelineNotice(Long pipelineRecordId, String type, List<Receiver> receivers, @Nullable Map<String, String> params);

    /**
     * 实例状态变更发送webhook josn
     */
    void sendInstanceStatusUpdate(AppServiceInstanceDTO appServiceInstanceDTO, DevopsEnvCommandDTO devopsEnvCommandDTO, String currentStatus);

    /**
     * 发送API测试告警通知
     *
     * @param userIds
     * @param params
     */
    void sendApiTestWarningMessage(Set<Long> userIds, Map<String, String> params, Long projectId);

    /**
     * 发送API测试套件告警通知
     *
     * @param userIds
     * @param params
     */
    void sendApiTestSuiteWarningMessage(Set<Long> userIds, Map<String, String> params, Long projectId);
}
