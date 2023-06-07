package io.choerodon.devops.app.eventhandler.constants;

/**
 * 此类放sagaTaskCode常量
 * Created by Sheep on 2019/7/16.
 */


public class SagaTaskCodeConstants {
    /**
     * devops创建环境
     */
    public static final String DEVOPS_CREATE_ENV = "devopsCreateEnv";

    /**
     * 环境创建失败
     */
    public static final String DEVOPS_CREATE_ENV_ERROR = "devopsCreateEnvError";

    /**
     * gitops事件处理
     */
    public static final String DEVOPS_GIT_OPS = "devopsGitOps";

    /**
     * 创建gitlab项目
     */
    public static final String DEVOPS_CREATE_APPLICATION_SERVICE = "devopsCreateApplicationService";

    /**
     * 创建外部gitlab项目
     */
    public static final String DEVOPS_CREATE_EXTERNAL_APPLICATION_SERVICE = "devopsCreateExternalApplicationService";

    /**
     * Devops从外部代码平台导入到gitlab项目
     */
    public static final String DEVOPS_CREATE_GITLAB_PROJECT = "devopsCreateGitlabProject";

    /**
     * GitOps 用户权限分配处理
     */
    public static final String DEVOPS_UPDATE_GITLAB_USERS = "devopsUpdateGitlabUsers";

    /**
     * GitOps 应用创建失败处理
     */
    public static final String DEVOPS_CREATE_GITLAB_PROJECT_ERROR = "devopsCreateGitlabProjectErr";

    /**
     * GitOps应用模板创建失败处理
     */
    public static final String DEVOPS_CREATE_GITLAB_PROJECT_TEMPLATE_ERROR = "devopsCreateGitlabProjectTemplateErr";

    /**
     * 模板事件处理
     */
    public static final String DEVOPS_OPERATION_GITLAB_TEMPLATE_PROJECT = "devopsOperationGitlabTemplateProject";

    /**
     * gitlab pipeline事件
     */
    public static final String DEVOPS_GITLAB_PIPELINE = "devopsGitlabPipeline";

    /**
     * gitlab pipeline事件
     */
    public static final String DEVOPS_GITLAB_CI_PIPELINE = "devopsGitlabCiPipeline";

    /**
     * gitlab pipeline事件
     */
    public static final String DEVOPS_GITLAB_CD_PIPELINE = "devopsGitlabCDPipeline";

    /**
     * 触发纯cd流水线
     */
    public static final String DEVOPS_TRIGGER_SIMPLE_CD_PIPELINE = "devopsTriggerSimpleCdPipeline";

    /**
     * 创建流水线自动部署实例
     */
    public static final String DEVOPS_PIPELINE_CREATE_INSTANCE = "devops-pipeline-create-instance";
    /**
     * 处理API测试任务执行完成后逻辑
     */
    public static final String HANDLE_API_TEST_TASK_COMPLETE_EVENT = "handle-api-test-task-complete-event";
    /**
     * 处理API测试套件执行完成后逻辑
     */
    public static final String HANDLE_API_TEST_SUITE_COMPLETE_EVENT = "handle-api-test-suite-complete-event";

    /**
     * 创建流水线环境自动部署实例
     */
    public static final String DEVOPS_PIPELINE_ENV_CREATE_INSTANCE = "devops-pipeline-env-create-instance";

    /**
     * 组织层创建用户
     */
    public static final String ORG_USER_CREAT = "iam-create-org-user";
    /**
     * devops创建分支
     */
    public static final String DEVOPS_CREATE_BRANCH = "devopsCreateBranch";

    /**
     * devops创建实例
     */
    public static final String DEVOPS_CREATE_INSTANCE = "devopsCreateInstance";

    /**
     * devops创建市场实例
     */
    public static final String DEVOPS_CREATE_MARKET_INSTANCE = "devops-process-market-instance";

    /**
     * devops创建网络
     */
    public static final String DEVOPS_CREATE_SERVICE = "devopsCreateService";

    /**
     * devops创建域名
     */
    public static final String DEVOPS_CREATE_INGRESS = "devopsCreateIngress";

    /**
     * devops创建PVC
     */
    public static final String DEVOPS_CREATE_PERSISTENTVOLUMECLAIM = "devopsCreatePersistentVolumeClaim";

    /**
     * devops创建PV
     */
    public static final String DEVOPS_CREATE_PERSISTENTVOLUME = "devopsCreatePersistentVolume";

    /**
     * 初始化Demo环境的项目相关数据
     */
    public static final String REGISTER_DEVOPS_INIT_DEMO_DATA = "register-devops-init-demo-data";

    /**
     * 创建对应项目的两个gitlab组
     */
    public static final String REGISTER_DEVOPS_INIT_PROJCET = "register-devops-init-projcet";

    /**
     * devops 创建 GitLab Group
     */
    public static final String DEVOPS_CREATE_GITLAB_GROUP = "devopsCreateGitLabGroup";

    /**
     * devops  更新 GitLab Group
     */
    public static final String DEVOPS_UPDATE_GITLAB_GROUP = "devopsUpdateGitLabGroup";


    /**
     * devops 创建 Harbor
     */
    public static final String DEVOPS_CREATE_HARBOR = "devopsCreateHarbor";

    /**
     * 创建组织事件
     */
    public static final String DEVOPS_CREATE_ORGANIZATION = "devopsCreateOrganization";

    /**
     * 创建应用事件
     */
    public static final String IAM_CREATE_APPLICATION = "iamCreateApplication";

    /**
     * Iam删除应用
     */
    public static final String IAM_DELETE_APPLICATION = "IamDeleteApplication";

    /**
     * Iam更新应用事件
     */
    public static final String IAM_UPDATE_APPLICATION = "iamUpdateApplication";

    /**
     * Iam启用应用事件
     */
    public static final String IAM_ENABLE_APPLICATION = "iamEnableApplication";

    /**
     * Iam停用应用事件
     */
    public static final String IAM_DISABLE_APPLICATION = "iamDisableApplication";

    /**
     * 更新角色同步事件
     */
    public static final String IAM_UPDATE_MEMBER_ROLE = "devopsUpdateMemberRole";

    /**
     * 删除角色同步事件
     */
    public static final String IAM_DELETE_MEMBER_ROLE = "devopsDeleteMemberRole";

    /**
     * 创建用户
     */
    public static final String IAM_CREATE_USER = "devopsCreateUser";

    /**
     * 更新用户
     */
    public static final String IAM_UPDATE_USER = "devopsUpdateUser";

    /**
     * 启用用户
     */
    public static final String IAM_ENABLE_USER = "devopsEnableUser";

    /**
     * 禁用用户
     */
    public static final String IAM_DISABLE_USER = "devopsDisableUser";

    /**
     * 应用上传
     */
    public static final String APIM_UPLOAD_APP = "apimUploadApplication";

    /**
     * 应用上传，修复版本
     */
    public static final String APIM_UPLOAD_APP_FIX_VERSION = "apimUploadApplicationFixVersion";

    /**
     * 应用下载
     */
    public static final String APIM_DOWNLOAD_APP = "apimDownloadApplication";


    /**
     * 在gitlab更新环境的权限
     */
    public static final String DEVOPS_UPDATE_ENV_PERMISSION = "devops-update-env-permission";


    /**
     * devops导入内部应用服务
     */
    public static final String DEVOPS_IMPORT_INTERNAL_APPLICATION_SERVICE = "devopsImportInternalAppService";

    /**
     * devops导入市场应用服务
     */
    public static final String DEVOPS_IMPORT_MARKET_APPLICATION_SERVICE = "devopsImportMarketAppService";
    /**
     * 删除环境
     */
    public static final String DEVOPS_DELETE_ENV = "devops-delete-env";
    /**
     * devops删除应用服务
     */
    public static final String DEVOPS_APP_DELETE = "devops-delete-app-service";

    /**
     * DevOps消费添加admin用户事件
     */
    public static final String DEVOPS_ADD_ADMIN = "devops-add-admin";

    /**
     * DevOps消费删除admin用户U事件
     */
    public static final String DEVOPS_DELETE_ADMIN = "devops-delete-admin";


    /**
     * DevOps消费批量部署事件
     */
    public static final String DEVOPS_BATCH_DEPLOYMENT = "devops-handle-batch-deployment";

    /**
     * devops 删除habor镜像
     */
    public static final String DEVOPS_DELETE_HABOR_IMAGE_TAGS = "devops-delete-habor-image-tags";
    /**
     * devops 删除chart versions
     */
    public static final String DEVOPS_DELETE_CHART_VERSIONS = "devops-delete-chart-versions";

    /**
     * 执行k8s安装命令
     */
    public static final String EXECUTE_INSTALL_K8S_COMMAND = "execute-install-k8s-command";

    /**
     * 检查节点
     */
    public static final String DEVOPS_NODE_CHECK = "devops-node-check";

    /**
     * 添加节点
     */
    public static final String DEVOPS_CLUSTER_ADD_NODE_TASK = "devops_cluster_add_node_task";

    /**
     * devops项目类型同步处理
     */
    public static final String DEVOPS_PROJECT_CATEGORY_SYNC = "devops-project-category-sync";

    /**
     * 创建应用模板
     */
    public static final String DEVOPS_CREATE_APP_TEMPLATE = "api-create-app-template";

    /**
     * 删除应用模板
     */
    public static final String DEVOPS_DELETE_APP_TEMPLATE = "api-delete-app-template";

    /**
     * 删除应用模板
     */
    public static final String DEVOPS_POD_READY_HANDLER_FOR_HZERO_DEPLOY = "devops-pod-ready-handler-for-hzero-deploy";

    /**
     * 迁移应用服务
     */
    public static final String DEVOPS_TRANSFER_APP_SERVICE = "devops-transfer-app-service";

    /**
     * 处理hzero实例部署失败
     */
    public static final String DEVOPS_HZERO_DEPLOY_FAILED = "devops-hzero-deploy-failed";

    /**
     * 创建流水线定时执行任务
     */
    public static final String DEVOPS_CREATE_PIPELINE_TIME_TASK = "devops-create-pipeline-time-task";

    public static final String DEVOPS_APP_VERSION_TRIGGER_PIPELINE = "devops-app-version-trigger-pipeline";

    public static final String DEVOPS_PIPELINE_JOB_FINISH = "devops-pipeline-job-finish";

    public static final String DEVOPS_SAVE_SONAR_ANALYSE_DATA = "devops-save-sonar-analyse-data";

    private SagaTaskCodeConstants() {
    }
}