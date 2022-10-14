package io.choerodon.devops.infra.constant;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/11 18:25
 */
public class ResourceCheckConstant {
    private ResourceCheckConstant() {

    }

    // app
    public static final String ERROR_APP_GROUP_ID_IS_NULL = "error.app.group.id.is.null";
    public static final String ERROR_APP_ARTIFACT_ID_IS_NULL = "error.app.artifact.id.is.null";
    public static final String ERROR_APP_SERVICE_ID_IS_NULL = "error.app.service.id.is.null";


    // app service version
    public static final String DEVOPS_SERVICE_VERSION_VALUE_ID_IS_NULL = "devops.service.version.value.id.is.null";
    public static final String DEVOPS_SERVICE_VERSION_ID_IS_NULL = "devops.service.version.id.is.null";


    public static final String ERROR_PARAM_IS_INVALID = "error.param.is.invalid";
    public static final String ERROR_USER_ID_IS_NULL = "error.user.id.is.null";

    // sonar
    public static final String ERROR_SONAR_SCANNER_TYPE_INVALID = "error.sonar.scanner.type.invalid";

    // project
    public static final String ERROR_PROJECT_ID_IS_NULL = "error.project.id.is.null";

    // env
    public static final String ERROR_COMMAND_ID_IS_NULL = "error.command.id.is.null";
    public static final String ERROR_ENV_ID_IS_NULL = "error.env.id.is.null";

    // instance
    public static final String ERROR_INSTANCE_NAME_IS_NULL = "error.instance.name.is.null";
    public static final String ERROR_INSTANCE_CODE_IS_NULL = "error.instance.code.is.null";
    public static final String ERROR_INSTANCE_ID_IS_NULL = "error.instance.id.is.null";

    // pod

    public static final String ERROR_POD_NAME_IS_NULL = "error.pod.name.is.null";

    // resource
    public static final String ERROR_KIND_NAME_IS_NULL = "error.kind.name.is.null";
    public static final String ERROR_RESOURCE_NAME_IS_NULL = "error.resource.name.is.null";


    // host
    public static final String ERROR_HOST_ID_IS_NULL = "error.host.id.is.null";
    public static final String ERROR_HOST_INSTANCE_ID_IS_NULL = "error.host.instance.id.is.null";
    public static final String ERROR_HOST_INSTANCE_TYPE_IS_NULL = "error.host.instance.type.is.null";
    public static final String ERROR_CONTAINER_NAME_IS_NULL = "error.container.name.is.null";
    public static final String ERROR_JAR_NAME_IS_NULL = "error.jar.name.is.null";
    public static final String ERROR_SOURCE_TYPE_IS_NULL = "error.source.type.is.null";


    // deploy
    public static final String ERROR_DEPLOY_RECORD_ID_IS_NULL = "error.deploy.record.id.is.null";

    // app
    public static final String ERROR_APP_CODE_IS_NULL = "error.app.code.is.null";
    public static final String ERROR_APP_ID_IS_NULL = "error.app.id.is.null";
    public static final String ERROR_APP_CONTAINER_ID_IS_NULL = "error.app.container.id.is.null";
    public static final String ERROR_APP_INSTANCE_IS_ASSOCIATED_WITH_PIPELINE = "error.app.instance.is.associated.with.pipeline";


    // pipeline
    public static final String ERROR_GITLAB_PIPELINE_ID_IS_NULL = "error.gitlab.pipeline.id.is.null";
    public static final String ERROR_JOB_NAME_ID_IS_NULL = "error.job.name.id.is.null";
    public static final String ERROR_JOB_TYPE_IS_NULL = "error.job.type.is.null";

    // general
    public static final String ERROR_RESOURCE_ID_IS_NULL = "error.resource.id.is.null";
    public static final String ERROR_RESOURCE_TYPE_IS_NULL = "error.resource.type.is.null";


}
