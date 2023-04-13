package io.choerodon.devops.api.vo.kubernetes;

import io.choerodon.devops.api.vo.kubernetes.certification.CertificationMetadata;
import io.choerodon.devops.api.vo.kubernetes.certification.CertificationSpec;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;


/**
 * Created by n!Ck
 * Date: 2018/8/20
 * Time: 16:55
 * Description:
 */
public class C7nCertification {
    public static final String API_VERSION_V1ALPHA1 = "certmanager.k8s.io/v1alpha1";
    public static final String API_VERSION_V1 = "cert-manager.io/v1";

    @ApiModelProperty("api版本")
    private String apiVersion;
    @ApiModelProperty("类型")
    private String kind;
    @ApiModelProperty("对象元数据")
    private CertificationMetadata metadata;
    @ApiModelProperty("对象配置内容")
    private CertificationSpec spec;

    public C7nCertification() {
    }

    public C7nCertification(String apiVersion) {
        this.apiVersion = apiVersion;
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
