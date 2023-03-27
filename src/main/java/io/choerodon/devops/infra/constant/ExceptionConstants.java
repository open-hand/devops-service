package io.choerodon.devops.infra.constant;

public class ExceptionConstants {

    public static class PublicCode {
        public static final String DEVOPS_CODE_EXIST = "devops.code.exist";
        public static final String DEVOPS_NAME_EXIST = "devops.name.exist";
        public static final String DEVOPS_RESOURCE_INSERT = "devops.resource.insert";
        public static final String DEVOPS_FIELD_NOT_SUPPORTED_FOR_SORT = "devops.field.not.supported.for.sort";
        public static final String DEVOPS_YAML_FORMAT_INVALID = "devops.yaml.format.invalid";
        public static final String DEVOPS_READ_MULTIPART_FILE = "devops.read.multipart.file";

        public static final String DEVOPS_DELETE_PERMISSION_OF_PROJECT_OWNER = "devops.delete.permission.of.project.owner";
        public static final String DEVOPS_ORGANIZATION_GET = "devops.organization.get";
        public static final String DEVOPS_ORGANIZATION_ROLE_ID_GET = "devops.organization.role.id.get";
        public static final String DEVOPS_CONTEXT_SET_ERROR = "devops.context.set.error";

        public static final String DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT = "devops.operating.resource.in.other.project";

    }

    /**
     * 应用服务相关
     */
    public static class AppServiceCode {
        public static final String DEVOPS_DELETE_NONFAILED_APP_SERVICE = "devops.delete.nonfailed.app.service";
        public static final String DEVOPS_DELETE_APPLICATION_SERVICE_DUE_TO_SHARE = "devops.delete.application.service.due.to.share";
        public static final String DEVOPS_DELETE_APPLICATION_SERVICE_DUE_TO_RESOURCES = "devops.delete.application.service.due.to.resources";
        public static final String DEVOPS_DELETE_APPLICATION_SERVICE_DUE_TO_CI_PIPELINE = "devops.delete.application.service.due.to.ci.pipeline";
        public static final String DEVOPS_DELETE_APP_SERVICE_DUE_TO_CI_PIPELINE = "devops.delete.app.service.due.to.ci.pipeline";
        public static final String DEVOPS_TEMPLATE_FIELDS = "devops.template.fields";
        public static final String DEVOPS_LOAD_CI_SH = "devops.load.ci.sh";
        public static final String DEVOPS_APP_PROJECT_NOT_MATCH = "devops.app.project.notMatch";
        public static final String DEVOPS_APP_IS_ALREADY_BIND = "devops.app.is.already.bind";
        public static final String DEVOPS_REPOSITORY_EMPTY = "devops.repository.empty";
        public static final String DEVOPS_REPOSITORY_ACCOUNT_INVALID = "devops.repository.account.invalid";
        public static final String DEVOPS_REPOSITORY_TOKEN_INVALID = "devops.repository.token.invalid";
        public static final String DEVOPS_LIST_DEPLOY_APP_SERVICE_TYPE = "devops.list.deploy.app.service.type";
        public static final String DEVOPS_APPLICATION_CREATE_INSERT = "devops.application.create.insert";
        public static final String DEVOPS_APP_SERVICE_UPDATE = "devops.app.service.update";
        public static final String DEVOPS_SOURCE_CODE_URL_IS_NULL = "devops.source.code.url.is.null";
        public static final String DEVOPS_SOURCE_CODE_VO_IS_NULL = "devops.source.code.vo.is.null: {}";
        public static final String DEVOPS_SONARQUBE_USER = "devops.sonarqube.user";
        public static final String DEVOPS_CHART_AUTHENTICATION_FAILED = "devops.chart.authentication.failed";
        public static final String DEVOPS_CHART_URL_BASE = "devops.chart.url.base";
        public static final String DEVOPS_CHART_NOT_AVAILABLE = "devops.chart.not.available";
        public static final String DEVOPS_INIT_APP_FROM_TEMPLATE_FAILED = "devops.init.app.from.template.failed";
        public static final String DEVOPS_TEMP_GIT_URL = "devops.temp.git.url";
        public static final String DEVOPS_DISABLE_OR_ENABLE_APPLICATION_SERVICE = "devops.disable.or.enable.application.service";
        public static final String DEVOPS_NOT_DELETE_SERVICE_BY_OTHER_PROJECT_DEPLOYMENT = "devops.not.delete.service.by.other.project.deployment";
        public static final String DEVOPS_APP_SERVICE_NOT_EXIST = "devops.app.service.not.exist";
        public static final String DEVOPS_APP_SERVICE_DISABLED = "devops.app.service.disabled";
        public static final String DEVOPS_GITLAB_PROJECT_ID_ASSOCIATED_WITH_OTHER_APP_SERVICE = "devops.gitlab.project.id.associated.with.other.app.service";
        public static final String DEVOPS_RENAME_FAIL = "devops.rename.fail";
        public static final String DEVOPS_APP_SERVICE_SYNC = "devops.app.service.sync";
        public static final String DEVOPS_USER_NOT_GITLAB_PROJECT_OWNER = "devops.user.not.gitlab.project.owner";
        public static final String DEVOPS_CREATE_PRIVATE_TOKEN = "devops.create.private.token";
        public static final String DEVOPS_TOKEN_INVALID = "devops.token.invalid";
        public static final String DEVOPS_APP_ID_NOT_EXIST = "devops.app.id.not.exist";
        public static final String DEVOPS_APP_NOT_IN_THIS_PROJECT = "devops.app.not.in.this.project";

    }

    public static class AppServiceInstanceCode {
        public static final String DEVOPS_INSTANCE_NOT_STOP = "devops.instance.not.stop";
        public static final String DEVOPS_INSTANCE_NOT_RUNNING = "devops.instance.not.running";
        public static final String DEVOPS_INSTANCE_RESOURCE_NOT_FOUND = "devops.instance.resource.not.found";

        public static final String DEVOPS_INSTANCE_JOB_MISMATCHED = "devops.instance.job.mismatched";
        public static final String DEVOPS_APP_INSTANCE_NAME_ALREADY_EXIST = "devops.app.instance.name.already.exist";
        public static final String DEVOPS_APPLICATION_INSTANCE_CREATE = "devops.application.instance.create";
        public static final String DEVOPS_INSTANCE_UPDATE = "devops.instance.update";
        public static final String DEVOPS_APP_INSTANCE_IS_OPERATING = "devops.app.instance.is.operating";
    }

    public static class AppServiceVersionCode {
        public static final String DEVOPS_VERSION_ID_NOT_EXIST = "devops.version.id.not.exist";
    }

    public static class AppServiceHelmVersionCode {
        public static final String DEVOPS_HELM_CONFIG_ID_NULL = "devops.helm.config.id.null";
        public static final String DEVOPS_HELM_CONFIG_NOT_EXIST = "devops.helm.config.not.exist";
    }

    public static class CiPipelineImageCode {
        public static final String DEVOPS_CREATE_IMAGE_RECORD = "devops.create.image.record";
        public static final String DEVOPS_UPDATE_IMAGE_RECORD = "devops.update.image.record";
    }

    public static class CiJobCode {
        public static final String DEVOPS_CI_JOB_DELAY_TIME_INVALID = "error.devops.ci.job.delay.time.invalid";

        public static final String DEVOPS_JOB_CONFIG_ID_IS_NULL = "error.devops.ci.job.config.id.null";
    }

    public static class SonarCode {
        public static final String DEVOPS_SONAR_QUALITY_GATE_CREATE = "error.devops.sonar.quality.gate.create";
        public static final String DEVOPS_SONAR_QUALITY_GATE_DELETE = "error.devops.sonar.quality.gate.delete";
        public static final String DEVOPS_SONAR_QUALITY_GATE_CONDITION_CREATE = "error.devops.sonar.quality.gate.condition.create";
        public static final String DEVOPS_SONAR_QUALITY_GATE_CONDITION_DELETE = "error.devops.sonar.quality.gate.condition.delete";
        public static final String DEVOPS_SONAR_QUALITY_GATE_CONDITION_VALUE_SHOULD_BE_GRATER_THAN_ZERO = "error.devops.sonar.quality.gate.condition.value.grater.than.zero";
        public static final String DEVOPS_SONAR_PROJECTS_SEARCH = "error.devops.sonar.projects.search";
        public static final String DEVOPS_SONAR_PROJECTS_CREATE = "error.devops.sonar.projects.create";
        public static final String Devops_SONAR_QUALITY_GATE_DETAILS_GET = "error.devops.sonar.quality.gate.details.get";
        public static final String DEVOPS_SONAR_QUALITY_GATE_SHOW_GET = "error.devops.sonar.quality.gate.show.get";
        public static final String DEVOPS_SONAR_QUALITY_GATE_BIND = "error.devops.sonar.quality.gate.bind";

    }

    public static class CdEnvDeployInfoDTOCode {
        public static final String DEVOPS_ENV_STOP_PIPELINE_APP_DEPLOY_EXIST = "devops.env.stop.pipeline.app.deploy.exist";
    }

    public static class CiHostDeployCode {
        public static final String DEVOPS_HOST_DEPLOY_INFO_CREATE = "error.devops.ci.host.deploy.info.save";
        public static final String DEVOPS_HOST_DEPLOY_INFO_NULL = "error.devops.ci.host.deploy.info.null";
        public static final String DEVOPS_HOST_DEPLOY_INFO_APP_NAME_OR_CODE_IS_NULL = "error.devops.ci.host.deploy.info.app.name.or.code.is.null";
        public static final String DEVOPS_HOST_DEPLOY_INFO_HOST_NULL = "error.devops.ci.host.deploy.info.host.is.null";
        public static final String DEVOPS_HOST_DEPLOY_INFO_PIPELINE_TASK_NULL = "error.devops.ci.host.deploy.info.pipeline.task.null";
        public static final String DEVOPS_UPDATE_PIPELINE_DOCKER_DEPLOY_INFO = "devops.update.pipeline.docker.deploy.info";
        public static final String DEVOPS_UPDATE_PIPELINE_JAR_DEPLOY_INFO = "devops.update.pipeline.jar.deploy.info";
        public static final String DEVOPS_UPDATE_PIPELINE_CUSTOM_DEPLOY_INFO = "devops.update.pipeline.custom.deploy.info";
    }

    public static class CiApiTestCode {
        public static final String DEVOPS_CI_API_TEST_INFO_SAVE = "error.devops.ci.api.info.save";
        public static final String DEVOPS_CI_API_TEST_INFO_TYPE_UNKNOWN = "error.devops.ci.api.info.type.unknown";
        public static final String DEVOPS_CI_API_TEST_INFO_NULL = "error.devops.ci.api.info.null";
        public static final String DEVOPS_CI_API_TEST_INFO_BOTH_TAK_ID_SUITE_ID_NULL = "error.devops.ci.api.info.both.task.id.suite.id.null";

    }

    public static class EnvCommandCode {
        public static final String DEVOPS_COMMAND_NOT_EXIST = "devops.command.not.exist";
    }

    public static class ClusterCode {
        public static final String DEVOPS_CLUSTER_NOT_EXIST = "devops.cluster.not.exist";
    }

    public static class CertificationCode {
        public static final String DEVOPS_CERTIFICATION_NOT_EXIST_IN_DATABASE = "devops.certification.not.exist.in.database";
    }

    public static class GitopsCode {
        public static final String DEVOPS_FILE_RESOURCE_NOT_EXIST = "devops.fileResource.not.exist";
        public static final String DEVOPS_FILE_CREATE = "devops.file.create";
        public static final String DEVOPS_FILE_UPDATE = "devops.file.update";
    }


    public static class EnvironmentCode {
        public static final String DEVOPS_ENV_ID_NOT_EXIST = "devops.env.id.not.exist";
    }

    public static class GitlabCode {
        public static final String DEVOPS_GITLAB_ACCESS_LEVEL = "devops.gitlab.access.level";
        public static final String DEVOPS_GITLAB_PROJECT_ID_IS_NULL = "devops.gitlab.project.id.is.null";
        public static final String DEVOPS_IAM_USER_SYNC_TO_GITLAB = "devops.iam.user.sync.to.gitlab";
        public static final String DEVOPS_GROUP_NOT_SYNC = "devops.group.not.sync";
        public static final String DEVOPS_GITLAB_GROUP_ID_SELECT = "devops.gitlab.groupId.select";
        public static final String DEVOPS_USER_NOT_GITLAB_OWNER = "devops.user.not.gitlab.owner";
        public static final String DEVOPS_USER_NOT_OWNER = "devops.user.not.owner";
        public static final String DEVOPS_BRANCH_GET = "devops.branch.get";
        public static final String DEVOPS_USER_NOT_IN_GITLAB_PROJECT = "devops.user.not.in.gitlab.project";
        public static final String DEVOPS_GIT_CHECKOUT = "devops.git.checkout";
        public static final String DEVOPS_GIT_PUSH = "devops.git.push";
        public static final String DEVOPS_DIRECTORY_DELETE = "devops.directory.delete";
        public static final String DEVOPS_TAGS_GET = "devops.tags.get";
        public static final String DEVOPS_BRANCH_CREATE = "devops.branch.create";
        public static final String DEVOPS_PROJECTHOOK_CREATE = "devops.projecthook.create";
        public static final String DEVOPS_QUERY_USER_BY_LOGIN_NAME = "devops.query.user.by.login.name";
        public static final String DEVOPS_GITLAB_USER_SYNC_FAILED = "devops.gitlab.user.sync.failed";
        public static final String DEVOPS_USER_GET = "devops.user.get";
        public static final String DEVOPS_JOB_QUERY = "devops.job.query";


    }

    public static class BranchCode {
        public static final String DEVOPS_BRANCH_EXIST = "devops.branch.exist";
    }

    public static class PVCode {
        public static final String DEVOPS_PV_NOT_EXISTS = "devops.pv.not.exists";
    }

    public static class WorkflowCode {
        public static final String DEVOPS_WORKFLOW_CREATE = "devops.workflow.create";
        public static final String DEVOPS_WORKFLOW_APPROVE = "devops.workflow.approve";
        public static final String DEVOPS_WORKFLOW_STOP = "devops.workflow.stop";
    }

    public static class DeployValueCode {
        public static final String DEVOPS_DEPLOY_VALUE_ID_NULL = "devops.deploy.value.id.null";
    }

    public static class CustomResourceCode {
        public static final String DEVOPS_LOAD_YAML_CONTENT = "devops.load.yaml.content";
    }

    public static class AppCode {
        public static final String DEVOPS_APP_CODE_IS_EMPTY = "devops.app.code.is.empty";
        public static final String DEVOPS_APP_NAME_IS_EMPTY = "devops.app.name.is.empty";
        public static final String DEVOPS_APP_ID_IS_EMPTY = "devops.app.id.is.empty";
        public static final String DEVOPS_APP_DEPLOY_TYPE_IS_EMPTY = "devops.app.deployType.is.empty";
    }

    public static class AppDeploy {
        public static final String DEVOPS_APP_DEPLOY_CONFIG_EMPTY = "devops.app.deploy.config.empty";
        public static final String DEVOPS_APP_SERVICE_ID_EMPTY = "devops.app.service.id.empty";
    }


    /**
     * 外置仓库配置相关
     */
    public static class AppExternalConfigServiceCode {
        public static final String DEVOPS_INVALID_APP_AUTH_TYPE = "devops.invalid.app.auth.type";
        public static final String DEVOPS_SAVE_APP_CONFIG_FAILED = "devops.save.app.config.failed";
        public static final String DEVOPS_UPDATE_APP_CONFIG_FAILED = "devops.update.app.config.failed";
    }

    public static class CertificationExceptionCode {
        public static final String ERROR_DEVOPS_CERTIFICATION_EXISTCERT_FILED_NULL = "error.devops.certification.existCert.filed.null";
        public static final String DEVOPS_CERTIFICATION_OPERATE_TYPE_NULL = "devops.certification.operateType.null";
    }

}
