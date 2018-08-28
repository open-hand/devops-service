package io.choerodon.devops.domain.application.valueobject.certification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
}
