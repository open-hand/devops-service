package io.choerodon.devops.infra.config;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by younger on 2018/3/29.
 */
@Component
@ConfigurationProperties(prefix = "ci")
public class CiYamlConfig {

    private Map<String, String> secrets;

    public Map<String, String> getSecrets() {
        return secrets;
    }

    public void setSecrets(Map<String, String> secrets) {
        this.secrets = secrets;
    }
}
