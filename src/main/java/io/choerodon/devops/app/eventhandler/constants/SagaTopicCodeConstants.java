package io.choerodon.devops.app.eventhandler.constants;

/**
 * * 此类放sagaTopicCode常量
 * Created by Sheep on 2019/7/16.
 */
public class SagaTopicCodeConstants {
    /**
     * devops创建gitlab模板项目
     */
    public static final String DEVOPS_CREATE_GITLAB_TEMPLATE_PROJECT = "devops-create-gitlab-template-project";

    /**
     * Devops设置创建应用模板状态失败
     */
    public static final String DEVOPS_SET_APPLICATION_TEMPLATE_ERROR = "devops-set-appTemplate-err";

    /**
     * Devops创建实例
     */
    public static final String DEVOPS_CREATE_INSTANCE = "devops-create-instance";

    /**
     * Devops创建市场实例
     */
    public static final String DEVOPS_CREATE_MARKET_INSTANCE = "devops-create-market-instance";

    /**
     * Devops创建网络
     */
    public static final String DEVOPS_CREATE_SERVICE = "devops-create-service";

    /**
     * Devops创建域名
     */
    public static final String DEVOPS_CREATE_INGRESS = "devops-create-ingress";

    /**
     * Devops创建PVC
     */
    public static final String DEVOPS_CREATE_PERSISTENTVOLUMECLAIM = "devops-create-persistentvolumeclaim";

    /**
     * Devops创建PV
     */
    public static final String DEVOPS_CREATE_PERSISTENTVOLUME = "devops-create-persistentvolume";

    /**
     * Devops创建应用服务
     */
    public static final String DEVOPS_CREATE_APPLICATION_SERVICE = "devops-create-application-service";

    /**
     * Devops创建外部应用服务
     */
    public static final String DEVOPS_CREATE_EXTERNAL_APPLICATION_SERVICE = "devops-create-external-application-service";

    /**
     * Devops删除应用服务版本
     */
    public static final String DEVOPS_DELETE_APPLICATION_SERVICE_VERSION = "devops-delete-application-service-version";

    /**
     * Devopsn导入应用服务（内部）
     */
    public static final String DEVOPS_IMPORT_INTERNAL_APPLICATION_SERVICE = "devops-import-internal-application-service";

    /**
     * Devopsn导入市场应用服务
     */
    public static final String DEVOPS_IMPORT_MARKET_APPLICATION_SERVICE = "devops-import-market-application-service";

    /**
     * Devops删除应用
     */
    public static final String DEVOPS_APP_DELETE = "devops-app-delete";

    /**
     * 同步应用服务状态
     */
    public static final String DEVOPS_APP_SYNC_STATUS = "devops-app-sync-status";

    /**
     * 同步iam应用状态
     */
    public static final String DEVOPS_SYNC_APP_ACTIVE = "devops-sync-app-active";

    /**
     * Devops设置application状态为创建失败
     */
    public static final String DEVOPS_CREATE_APP_FAIL = "devops-create-app-fail";

    /**
     * Devops从外部代码平台导入到gitlab项目
     */
    public static final String DEVOPS_IMPORT_GITLAB_PROJECT = "devops-import-gitlab-project";

    /**
     * Devops创建gitlab项目
     */
    public static final String DEVOPS_CREATE_GITLAB_PROJECT = "devops-create-gitlab-project";

    /**
     * 创建环境
     */
    public static final String DEVOPS_CREATE_ENV = "devops-create-env";

    /**
     * devops创建环境失败(devops set env status create err)
     */
    public static final String DEVOPS_SET_ENV_ERR = "devops-set-env-err";

    /**
     * gitlab pipeline创建到数据库
     */
    public static final String DEVOPS_GITLAB_PIPELINE = "devops-gitlab-pipeline";

    /**
     * gitlab ci pipeline创建到数据库
     */
    public static final String DEVOPS_GITLAB_CI_PIPELINE = "devops-gitlab-ci-pipeline";

    /**
     * 处理流水线执行成功， 为了纯cd流水线触发
     */
    public static final String DEVOPS_CI_PIPELINE_SUCCESS_FOR_SIMPLE_CD = "devops-ci-pipeline-success-for-simple-cd";

    /**
     * 创建分支
     */
    public static final String DEVOPS_CREATE_BRANCH = "devops-create-branch";

    /**
     * 处理GitOps
     */
    public static final String DEVOPS_SYNC_GITOPS = "devops-sync-gitops";


    /**
     * 测试应用Pod升级
     */
    public static final String TEST_POD_UPDATE_SAGA = "test-pod-update-saga";

    /**
     * 测试Job日志
     */
    public static final String TEST_JOB_LOG_SAGA = "test-job-log-saga";

    /**
     * 测试应用Release状态
     */
    public static final String TEST_STATUS_SAGA = "test-status-saga";

    /**
     * 注册组织事件
     */
    public static final String REGISTER_ORG = "register-org";

    /**
     * iam服务创建项目
     */
    public static final String IAM_CREATE_PROJECT = "iam-create-project";

    /**
     * iam服务更新项目
     */
    public static final String IAM_UPDATE_PROJECT = "iam-update-project";

    /**
     * base服务更新应用
     */
    public static final String BASE_UPDATE_APPLICATION = "base-update-application";

    /**
     * Iam删除应用
     */
    public static final String IAM_DELETE_APPLICATION = "iam-delete-application";

    /**
     * Iam更新应用事件
     */
    public static final String IAM_UPDATE_APPLICATION = "iam-update-application";

    /**
     * Iam启用应用事件
     */
    public static final String IAM_ENABLE_APPLICATION = "iam-enable-application";

    /**
     * Iam停用应用事件
     */
    public static final String IAM_DISABLE_APPLICATION = "iam-disable-application";

    /**
     * IAM更新角色
     */
    public static final String IAM_UPDATE_MEMBER_ROLE = "iam-update-memberRole";

    /**
     * IAM删除角色
     */
    public static final String IAM_DELETE_MEMBER_ROLE = "iam-delete-memberRole";

    /**
     * IAM创建用户
     */
    public static final String IAM_CREATE_USER = "iam-create-user";

    /**
     * IAM更新用户
     */
    public static final String IAM_UPDATE_USER = "iam-update-user";

    /**
     * IAM启用用户
     */
    public static final String IAM_ENABLE_USER = "iam-enable-user";

    /**
     * IAM禁用用户
     */
    public static final String IAM_DISABLE_USER = "iam-disable-user";

    /**
     * 更新环境的权限
     */
    public static final String DEVOPS_UPDATE_ENV_PERMISSION = "devops-update-env-permission";
    /**
     * 删除环境
     */
    public static final String DEVOPS_DELETE_ENV = "devops-delete-env";


    /**
     * 批量为用户分配Root权限
     */
    public static final String ASSIGN_ADMIN = "base-assign-admin";

    /**
     * 删除单个用户Root权限
     */
    public static final String DELETE_ADMIN = "base-delete-admin";

    /**
     * 批量部署
     */
    public static final String DEVOPS_BATCH_DEPLOYMENT = "devops-batch-deployment";

    /**
     * 创建集群
     */
    public static final String DEVOPS_INSTALL_K8S = "devops-install-k8s";

    /**
     * 重试创建集群
     */
    public static final String DEVOPS_RETRY_INSTALL_K8S = "devops-retry-install-k8s";

    /**
     * 添加集群节点
     */
    public static final String DEVOPS_CLUSTER_ADD_NODE = "devops_cluster_add_node";

    /**
     * 创建流水线自动部署实例
     */
    public static final String DEVOPS_PIPELINE_AUTO_DEPLOY_INSTANCE = "devops-pipeline-auto-deploy-instance";

    /**
     * API测试任务执行完成事件
     */
    public static final String API_TEST_TASK_COMPLETE_EVENT = "api-test-task-complete-event";

    /**
     * API测试套件执行完成事件
     */
    public static final String API_TEST_SUITE_COMPLETE_EVENT = "api-test-suite-complete-event";

    /**
     * 创建应用模板
     */
    public static final String DEVOPS_CREATE_APP_TEMPLATE = "api-create-app-template";

    /**
     * 删除应用模板
     */
    public static final String DEVOPS_DELETE_APP_TEMPLATE = "api-delete-app-template";

    /**
     * 删除tag
     */
    public static final String DEVOPS_GIT_TAG_DELETE = "devops-git-tag-delete";

    /**
     * 合并请求通过
     */
    public static final String DEVOPS_MERGE_REQUEST_PASS = "devops-merge-request-pass";

    /**
     * pod状态更新
     */
    public static final String DEVOPS_POD_READY = "devops-pod-ready";

    /**
     * 实例部署失败
     */
    public static final String DEVOPS_DEPLOY_FAILED = "devops-deploy-failed";

    /**
     * 迁移应用服务
     */
    public static final String DEVOPS_TRANSFER_APP_SERVICE = "devops-transfer-app-service";

    /**
     * 创建流水线定时执行任务
     */
    public static final String DEVOPS_CREATE_PIPELINE_TIME_TASK = "devops-create-pipeline-time-task";

    public static final String DEVOPS_APP_VERSION_TRIGGER_PIPELINE = "devops-app-version-trigger-pipeline";

    public static final String DEVOPS_PIPELINE_JOB_FINISH = "devops-pipeline-job-finish";


    private SagaTopicCodeConstants() {
    }
}
