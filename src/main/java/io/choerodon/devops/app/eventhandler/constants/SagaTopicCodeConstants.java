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
     * Devops创建应用
     */
    public static final String DEVOPS_CREATE_APPLICATION = "devops-create-application";

    /**
     * Devops删除失败应用
     */
    public static final String DEVOPS_APP_DELETE = "devops-app-delete";

    /**
     * Devops更新gitlab用户
     */
    public static final String DEVOPS_UPDATE_GITLAB_USERS = "devops-update-gitlab-users";

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
     * 创建流水线自动部署实例
     */
    public static final String DEVOPS_PIPELINE_AUTO_DEPLOY_INSTANCE = "devops-pipeline-auto-deploy-instance";

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
     * 创建组织事件
     */
    public static final String DEVOPS_CREATE_ORGANIZATION = "devopsCreateOrganization";

    /**
     * Iam创建应用事件
     */
    public static final String IAM_CREATE_APPLICATION = "iam-create-application";

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
}
