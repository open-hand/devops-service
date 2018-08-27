package io.choerodon.devops.infra.common.util.enums;

public enum GitOpsObjectError {

    RELEASE_META_DATA_NOT_FOUND("The C7nHelmRelease does not define metadata properties"),
    RELEASE_NAME_NOT_FOUND("The C7nHelmRelease does not define name properties"),
    RELEASE_SPEC_NOT_FOUND("The C7nHelmRelease does not define spec properties"),
    RELEASE_CHART_NAME_NOT_FOUND("The C7nHelmRelease does not define chartName properties in spec"),
    RELEASE_CHART_VERSION_NOT_FOUND("The C7nHelmRelease does not define chartVersion properties in spec"),
    RELEASE_REPOURL_NOT_FOUND("The C7nHelmRelease does not define repoUrl properties in spec"),
    RELEASE_APIVERSION_NOT_FOUND("The C7nHelmRelease does not define apiVersion properties"),
    SERVICE_METADATA_NOT_FOUND("The V1service does not define metadata properties"),
    SERVICE_NAME_NOT_FOUND("The V1service does not define name properties in metadata"),
    SERVICE_SPEC_NOT_FOUND("The V1service does not define spec properties"),
    SERVICE_PORTS_NOT_FOUND("The V1service does not define ports properties in spec"),
    SERVICE_PORTS_NAME_NOT_FOUND("The V1service does not define name properties in ports"),
    SERVICE_PORTS_PORT_NOT_FOUND("The V1service does not define port properties in ports"),
    SERVICE_PORTS_TARGET_PORT("The V1service does not define target properties in ports"),
    SERVICE_TYPE_NOT_FOUND("The V1service does not define type properties in spec"),
    SERVICE_APIVERSION_NOT_FOUND("The V1service does not define apiVersion properties"),
    INGRESS_META_DATA_NOT_FOUND("The v1beta1Ingress does not define metadata properties"),
    INGRESS_NAME_NOT_FOUND("The v1beta1Ingress does not define name properties in metadata"),
    INGRESS_SPEC_NOT_FOUND("The v1beta1Ingress does not define spec properties"),
    INGRESS_RULES_NOT_FOUND("The v1beta1Ingress does not define rules properties in spec"),
    INGRESS_RULE_HOST_NOT_FOUND("The v1beta1Ingress does not define host properties in rule"),
    INGRESS_RULE_HTTP_NOT_FOUND("The v1beta1Ingress does not define http properties in rule"),
    INGRESS_PATHS_NOT_FOUND("The v1beta1Ingress does not define paths properties in http"),
    INGRESS_PATHS_PATH_NOT_FOUND("The v1beta1Ingress does not define path properties in paths"),
    INGRESS_PATHS_BACKEND_NOT_FOUND("The v1beta1Ingress does not define backend properties in paths"),
    INGRESS_BACKEND_SERVICE_NAME_NOT_FOUND("The v1beta1Ingress does not define serviceName properties in backend"),
    INGRESS_BACKEND_SERVICE_PORT_NOT_FOUND("The v1beta1Ingress does not define servicePort properties in backend"),
    INGRESS_APIVERSION_NOT_FOUND("The v1beta1Ingress does not define apiVersion properties"),
    INGRESS_PATH_DUPLICATED("the ingress path is duplicated"),
    INSTANCE_APP_ID_NOT_SAME("The instance is not belong to the same application"),
    SERVICE_RELEATED_INGRESS_NOT_FOUND("the related service of the ingress not exist:"),
    INGRESS_PATH_PORT_NOT_BELONG_TO_SERVICE("the ingress path's port '{}' does not belong to service '{}'"),
    INSTANCE_RELEATED_SERVICE_NOT_FOUND("The related instance of the service not found: "),
    OBJECT_EXIST("the another file already has the same object: "),
    INGRESS_PATH_IS_EMPTY("the ingress path is empty!"),
    INGRESS_DOMAIN_PATH_IS_EXIST("the ingress domain and path is already exist!");

    private String error;

    GitOpsObjectError(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

}
