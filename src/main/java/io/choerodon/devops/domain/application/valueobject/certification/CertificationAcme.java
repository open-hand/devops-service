package io.choerodon.devops.domain.application.valueobject.certification;

import java.util.ArrayList;
import java.util.List;

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
}
