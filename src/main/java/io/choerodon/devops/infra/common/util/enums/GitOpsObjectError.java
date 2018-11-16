package io.choerodon.devops.infra.common.util.enums;

public enum GitOpsObjectError {
    RELEASE_META_DATA_NOT_FOUND("release.meta.data.not.found"),
    RELEASE_NAME_NOT_FOUND("release.name.not.found"),
    RELEASE_SPEC_NOT_FOUND("release.spec.not.found"),
    RELEASE_CHART_NAME_NOT_FOUND("release.chart.name.not.found"),
    RELEASE_CHART_VERSION_NOT_FOUND("release.chart.version.not.found"),
    RELEASE_REPO_URL_NOT_FOUND("release.repo.url.not.found"),
    RELEASE_API_VERSION_NOT_FOUND("release.api.version.not.found"),
    SERVICE_METADATA_NOT_FOUND("service.metadata.not.found"),
    SERVICE_NAME_NOT_FOUND("service.name.not.found"),
    SERVICE_SPEC_NOT_FOUND("service.spec.not.found"),
    SERVICE_PORTS_NOT_FOUND("service.ports.not.found"),
    SERVICE_PORTS_NAME_NOT_FOUND("service.ports.name.not.found"),
    SERVICE_PORTS_PORT_NOT_FOUND("service.ports.port.not.found"),
    SERVICE_PORTS_TARGET_PORT("service.ports.target.port"),
    SERVICE_TYPE_NOT_FOUND("service.type.not.found"),
    SERVICE_API_VERSION_NOT_FOUND("service.api.version.not.found"),
    SERVICE_RELATED_INGRESS_NOT_FOUND("service.related.ingress.not.found"),
    INGRESS_META_DATA_NOT_FOUND("ingress.meta.data.not.found"),
    INGRESS_NAME_NOT_FOUND("ingress.name.not.found"),
    INGRESS_SPEC_NOT_FOUND("ingress.spec.not.found"),
    INGRESS_RULES_NOT_FOUND("ingress.rules.not.found"),
    INGRESS_RULE_HOST_NOT_FOUND("ingress.rule.host.not.found"),
    INGRESS_RULE_HTTP_NOT_FOUND("ingress.rule.http.not.found"),
    INGRESS_BACKEND_SERVICE_NAME_NOT_FOUND("ingress.backend.service.name.not.found"),
    INGRESS_BACKEND_SERVICE_PORT_NOT_FOUND("ingress.backend.service.port.not.found"),
    INGRESS_API_VERSION_NOT_FOUND("ingress.api.version.not.found"),
    INGRESS_DOMAIN_PATH_IS_EXIST("ingress.domain.path.is.exist"),
    INGRESS_PATHS_NOT_FOUND("ingress.paths.not.found"),
    INGRESS_PATHS_PATH_NOT_FOUND("ingress.paths.path.not.found"),
    INGRESS_PATHS_BACKEND_NOT_FOUND("ingress.paths.backend.not.found"),
    INGRESS_PATH_DUPLICATED("ingress.path.duplicated"),
    INGRESS_PATH_IS_EMPTY("ingress.path.is.empty"),
    INGRESS_PATH_PORT_NOT_BELONG_TO_SERVICE("ingress.path.port.not.belong.to.service"),
    CERT_ACME_OR_EXIST_CERT_NOT_FOUND("cert.acme.or.exist.cert.not.found"),
    CERT_DOMAINS_ILLEGAL("cert.domains.illegal"),
    CERT_NAMESPACE_NOT_FOUND("cert.namespace.not.found"),
    CERT_API_VERSION_NOT_FOUND("cert.api.version.not.found"),
    CERT_META_DATA_NOT_FOUND("cert.meta.data.not.found"),
    CERT_NAME_NOT_FOUND("cert.name.not.found"),
    CERT_SPEC_NOT_FOUND("cert.spec.not.found"),
    CERT_COMMON_NAME_NOT_FOUND("cert.common.name.not.found"),
    CERT_CRT_NOT_FOUND("cert.crt.not.found"),
    CERT_KEY_NOT_FOUND("cert.key.not.found"),
    CERT_ACME_CONFIG_NOT_FOUND("cert.acme.config.not.found"),
    CERT_HTTP_NOT_FOUND("cert.http.not.found"),
    CERT_INGRESS_CLASS_ERROR("cert.ingress.class.error"),
    CERT_DOMAINS_NOT_FOUND("cert.domains.not.found"),
    CERT_CHANGED("cert.changed"),
    INSTANCE_APP_ID_NOT_SAME("instance.app.id.not.same"),
    INSTANCE_RELATED_SERVICE_NOT_FOUND("instance.related.service.not.found"),
    OBJECT_EXIST("object.exist");

    private String error;

    GitOpsObjectError(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

}
