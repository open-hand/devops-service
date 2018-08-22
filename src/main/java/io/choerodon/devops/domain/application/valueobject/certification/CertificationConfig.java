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
    private static final Map<String, String> http = new HashMap();
    private List<String> domains;

    static {
        http.put("ingressClass", "nginx");
    }

    public CertificationConfig() {
        this.domains = new ArrayList<>();
    }

    public CertificationConfig(List<String> domains) {
        this.domains = domains;
    }

    public static Map<String, String> getHttp() {
        return http;
    }

    public List<String> getDomains() {
        return domains;
    }

    public void setDomains(List<String> domains) {
        this.domains = domains;
    }
}
