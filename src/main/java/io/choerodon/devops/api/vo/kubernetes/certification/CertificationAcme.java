package io.choerodon.devops.api.vo.kubernetes.certification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by n!Ck
 * Date: 2018/8/20
 * Time: 16:59
 * Description:
 */
public class CertificationAcme {
    private List<CertificationConfig> config;

    public List<CertificationConfig> getConfig() {
        return config;
    }

    public void setConfig(List<CertificationConfig> config) {
        this.config = config;
    }

    public void initConfig(CertificationConfig config) {
        this.config = new ArrayList<>();
        this.config.add(config);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CertificationAcme)) {
            return false;
        }
        CertificationAcme acme = (CertificationAcme) o;
        return Objects.equals(getConfig(), acme.getConfig());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getConfig());
    }
}
