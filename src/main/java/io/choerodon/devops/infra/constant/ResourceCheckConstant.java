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


    // app service version
    public static final String ERROR_SERVICE_VERSION_VALUE_ID_IS_NULL = "error.service.version.value.id.is.null";

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

    // pod

    public static final String ERROR_POD_NAME_IS_NULL = "error.pod.name.is.null";

    // resource
    public static final String ERROR_KIND_NAME_IS_NULL = "error.kind.name.is.null";
    public static final String ERROR_RESOURCE_NAME_IS_NULL = "error.resource.name.is.null";


    // host
    public static final String ERROR_HOST_ID_IS_NULL = "error.host.id.is.null";
}
