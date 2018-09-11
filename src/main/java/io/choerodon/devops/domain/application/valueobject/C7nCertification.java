package io.choerodon.devops.domain.application.valueobject;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof C7nCertification)) {
            return false;
        }
        C7nCertification that = (C7nCertification) o;
        return Objects.equals(getApiVersion(), that.getApiVersion())
                && Objects.equals(getKind(), that.getKind())
                && Objects.equals(getMetadata(), that.getMetadata())
                && Objects.equals(getSpec(), that.getSpec());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getApiVersion(), getKind(), getMetadata(), getSpec());
    }
}
