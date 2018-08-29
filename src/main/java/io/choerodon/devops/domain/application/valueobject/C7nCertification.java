package io.choerodon.devops.domain.application.valueobject;

import io.choerodon.devops.domain.application.valueobject.certification.CertificationMetadata;
import io.choerodon.devops.domain.application.valueobject.certification.CertificationSpec;

/**
 * Created by n!Ck
 * Date: 2018/8/20
 * Time: 16:55
 * Description:
 */
public class C7nCertification {
    private String apiVersion;
    private String kind;
    private CertificationMetadata metadata;
    private CertificationSpec spec;

    public C7nCertification() {
        this.apiVersion = "certmanager.k8s.io/v1alpha1";
        this.kind = "Certificate";
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public String getKind() {
        return kind;
    }

    public CertificationMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(CertificationMetadata metadata) {
        this.metadata = metadata;
    }

    public CertificationSpec getSpec() {
        return spec;
    }

    public void setSpec(CertificationSpec spec) {
        this.spec = spec;
    }
}
