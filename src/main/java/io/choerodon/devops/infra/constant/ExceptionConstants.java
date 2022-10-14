package io.choerodon.devops.infra.constant;

public class ExceptionConstants {

    public static class PublicCode {
        public static final String DEVOPS_CODE_EXIST = "devops.code.exist";
        public static final String DEVOPS_NAME_EXIST = "devops.name.exist";
        public static final String DEVOPS_RESOURCE_INSERT = "devops.resource.insert";
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

    }

    public static class AppServiceInstanceCode {
        public static final String DEVOPS_INSTANCE_NOT_STOP = "devops.instance.not.stop";
        public static final String DEVOPS_INSTANCE_NOT_RUNNING = "devops.instance.not.running";
        public static final String DEVOPS_INSTANCE_RESOURCE_NOT_FOUND = "devops.instance.resource.not.found";
        public static final String DEVOPS_APP_INSTANCE_NAME_ALREADY_EXIST = "devops.app.instance.name.already.exist";
        public static final String DEVOPS_APPLICATION_INSTANCE_CREATE = "devops.application.instance.create";
        public static final String DEVOPS_INSTANCE_UPDATE = "devops.instance.update";
        public static final String DEVOPS_APP_INSTANCE_IS_OPERATING = "devops.app.instance.is.operating";
    }

    public static class AppServiceVersionCode {
        public static final String DEVOPS_VERSION_ID_NOT_EXIST = "devops.version.id.not.exist";
    }

    public static class EnvCommandCode {
        public static final String DEVOPS_COMMAND_NOT_EXIST = "devops.command.not.exist";
    }


    public static class EnvironmentCode {
        public static final String DEVOPS_ENV_ID_NOT_EXIST = "devops.env.id.not.exist";
    }

    /**
     * 外置仓库配置相关
     */
    public static class AppExternalConfigServiceCode {
        public static final String DEVOPS_INVALID_APP_AUTH_TYPE = "devops.invalid.app.auth.type";
        public static final String DEVOPS_SAVE_APP_CONFIG_FAILED = "devops.save.app.config.failed";
        public static final String DEVOPS_UPDATE_APP_CONFIG_FAILED = "devops.update.app.config.failed";
    }

}
