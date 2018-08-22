package io.choerodon.devops.domain.application.valueobject.certification;

/**
 * Created by n!Ck
 * Date: 2018/8/20
 * Time: 17:12
 * Description:
 */
public class CertificationExistCert {
    private String key;
    private String cert;

    public CertificationExistCert() {
    }

    public CertificationExistCert(String key, String cert) {
        this.key = key;
        this.cert = cert;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCert() {
        return cert;
    }

    public void setCert(String cert) {
        this.cert = cert;
    }
}
