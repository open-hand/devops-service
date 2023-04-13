package io.choerodon.devops.api.vo.kubernetes.certification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.infra.enums.CertificationType;

/**
 * Created by n!Ck
 * Date: 2018/8/20
 * Time: 16:57
 * Description:
 */

public class CertificationSpec {
    public static final String LETSENCRYPT_PROD = "letsencrypt-prod";
    public static final String LOCALHOST = "localhost";
    public static final String CLUSTER_ISSUER = "ClusterIssuer";
    @ApiModelProperty("泛域名")
    private String commonName;
    @ApiModelProperty("子域名")
    private List<String> dnsNames;
    @ApiModelProperty("自动证书管理环境")
    private CertificationAcme acme;
    @ApiModelProperty("证书")
    private CertificationExistCert existCert;
    @ApiModelProperty("证书生成的secret名称")
    private String secretName;
    @ApiModelProperty("证书颁发者")
    private Map<String, String> issuerRef;

    /**
     * empty construct
     */
    public CertificationSpec() {
    }

    /**
     * construct test param
     */
    public CertificationSpec(String type) {
        issuerRef = new HashMap<>();
        if (type.equals(CertificationType.REQUEST.getType())) {
            issuerRef.put("name", LETSENCRYPT_PROD);
            issuerRef.put("kind", CLUSTER_ISSUER);
        } else if (type.equals(CertificationType.UPLOAD.getType()) || type.equals(CertificationType.CHOOSE.getType())) {
            issuerRef.put("name", LOCALHOST);
            issuerRef.put("kind", CLUSTER_ISSUER);
        }
    }

    public Map<String, String> getIssuerRef() {
        return issuerRef;
    }

    public void setIssuerRef(Map<String, String> issuerRef) {
        this.issuerRef = issuerRef;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public List<String> getDnsNames() {
        return dnsNames;
    }

    public void setDnsNames(List<String> dnsNames) {
        this.dnsNames = dnsNames;
    }

    public CertificationAcme getAcme() {
        return acme;
    }

    public void setAcme(CertificationAcme acme) {
        this.acme = acme;
    }

    public CertificationExistCert getExistCert() {
        return existCert;
    }

    public void setExistCert(CertificationExistCert existCert) {
        this.existCert = existCert;
    }

    public String getSecretName() {
        return secretName;
    }

    public void setSecretName(String secretName) {
        this.secretName = secretName;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (!(o instanceof CertificationSpec)) {
            return false;
        }
        CertificationSpec that = (CertificationSpec) o;
        return Objects.equals(getCommonName(), that.getCommonName())
                && Objects.equals(getSecretName(), that.getSecretName())
                && Objects.equals(getDnsNames(), that.getDnsNames())
                && Objects.equals(getAcme(), that.getAcme())
                && Objects.equals(getExistCert(), that.getExistCert())
                && Objects.equals(getIssuerRef(), that.getIssuerRef());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCommonName(), getSecretName(), getDnsNames(), getAcme(), getExistCert(), getIssuerRef());
    }
}
