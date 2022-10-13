package io.choerodon.devops.infra.constant;

public class ExceptionConstants {

    public static class PublicCode {
        public static final String ERROR_CODE_EXIST = "error.code.exist";
        public static final String ERROR_NAME_EXIST = "error.name.exist";
    }

    /**
     * 应用服务相关
     */
    public static class AppServiceCode {
        public static final String ERROR_DELETE_NONFAILED_APP_SERVICE = "error.delete.nonfailed.app.service";
        public static final String ERROR_DELETE_APPLICATION_SERVICE_DUE_TO_SHARE = "error.delete.application.service.due.to.share";
        public static final String ERROR_DELETE_APPLICATION_SERVICE_DUE_TO_RESOURCES = "error.delete.application.service.due.to.resources";
        public static final String ERROR_DELETE_APPLICATION_SERVICE_DUE_TO_CI_PIPELINE = "error.delete.application.service.due.to.ci.pipeline";
        public static final String ERROR_DELETE_APP_SERVICE_DUE_TO_CI_PIPELINE = "error.delete.app.service.due.to.ci.pipeline";
        public static final String ERROR_TEMPLATE_FIELDS = "error.template.fields";
        public static final String ERROR_LOAD_CI_SH = "error.load.ci.sh";
        public static final String ERROR_APP_PROJECT_NOTMATCH = "error.app.project.notMatch";
        public static final String ERROR_APP_IS_ALREADY_BIND = "error.app.is.already.bind";
        public static final String ERROR_REPOSITORY_EMPTY = "error.repository.empty";
        public static final String ERROR_REPOSITORY_ACCOUNT_INVALID = "error.repository.account.invalid";
        public static final String ERROR_REPOSITORY_TOKEN_INVALID = "error.repository.token.invalid";
        public static final String ERROR_LIST_DEPLOY_APP_SERVICE_TYPE = "error.list.deploy.app.service.type";
        public static final String ERROR_APPLICATION_CREATE_INSERT = "error.application.create.insert";
        public static final String ERROR_APP_SERVICE_UPDATE = "error.app.service.update";
        public static final String ERROR_SOURCE_CODE_URL_IS_NULL = "error.source.code.url.is.null";
        public static final String ERROR_SOURCE_CODE_VO_IS_NULL = "error.source.code.vo.is.null: {}";
        public static final String ERROR_SONARQUBE_USER = "error.sonarqube.user";
        public static final String ERROR_CHART_AUTHENTICATION_FAILED = "error.chart.authentication.failed";
        public static final String ERROR_CHART_URL_BASE = "error.chart.url.base";
        public static final String ERROR_CHART_NOT_AVAILABLE = "error.chart.not.available";
        public static final String ERROR_INIT_APP_FROM_TEMPLATE_FAILED = "error.init.app.from.template.failed";
        public static final String ERROR_TEMP_GIT_URL = "error.temp.git.url";
        public static final String ERROR_DISABLE_OR_ENABLE_APPLICATION_SERVICE = "error.disable.or.enable.application.service";
        public static final String ERROR_NOT_DELETE_SERVICE_BY_OTHER_PROJECT_DEPLOYMENT = "error.not.delete.service.by.other.project.deployment";

    }

    /**
     * 外置仓库配置相关
     */
    public static class AppExternalConfigServiceCode {
        public static final String ERROR_INVALID_APP_AUTH_TYPE = "devops.invalid.app.auth.type";
        public static final String ERROR_SAVE_APP_CONFIG_FAILED = "devops.save.app.config.failed";
        public static final String ERROR_UPDATE_APP_CONFIG_FAILED = "devops.update.app.config.failed";
    }

}
