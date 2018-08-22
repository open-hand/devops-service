package io.choerodon.devops.domain.application.valueobject.certification;

import java.util.List;

/**
 * Created by n!Ck
 * Date: 2018/8/20
 * Time: 16:57
 * Description:
 */

public class CertificationSpec {
    private String commonName;
    private List<String> dnsNames;
    private CertificationAcme acme;
    private CertificationExistCert existCert;

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
}
