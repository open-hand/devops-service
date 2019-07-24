package io.choerodon.devops.api.vo.kubernetes.certification;

import java.util.*;

/**
 * Created by n!Ck
 * Date: 2018/8/20
 * Time: 16:59
 * Description:
 */
public class CertificationConfig {
    private Map<String, String> http01;
    private List<String> domains;

    public CertificationConfig() {
        this.domains = new ArrayList<>();
        this.http01 = new HashMap<>();
        this.http01.put("ingressClass", "nginx");
    }

    public CertificationConfig(List<String> domains) {
        this.domains = domains;
        this.http01 = new HashMap<>();
        this.http01.put("ingressClass", "nginx");
    }

    public void setHttp01(Map<String, String> http01) {
        this.http01 = http01;
    }

    public Map<String, String> getHttp01() {
        return http01;
    }

    public List<String> getDomains() {
        return domains;
    }

    public void setDomains(List<String> domains) {
        this.domains = domains;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CertificationConfig)) {
            return false;
        }
        CertificationConfig config = (CertificationConfig) o;
        return Objects.equals(getHttp01(), config.getHttp01())
                && Objects.equals(getDomains(), config.getDomains());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getHttp01(), getDomains());
    }
}
