package io.choerodon.devops.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "services.gitlab")
public class GitlabConfigurationProperties {

    private String password;
    private Integer projectLimit;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getProjectLimit() {
        return projectLimit;
    }

    public void setProjectLimit(Integer projectLimit) {
        this.projectLimit = projectLimit;
    }
}
