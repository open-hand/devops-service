package io.choerodon.devops.infra.config;

import io.choerodon.devops.api.vo.ConfigVO;

public class ConfigurationProperties extends HarborConfigurationProperties {

    private String type;


    public ConfigurationProperties() {

    }

    public ConfigurationProperties(ConfigVO config) {
        this.setBaseUrl(config.getUrl());
        this.setProject(config.getProject());
        this.setUsername(config.getUserName());
        this.setPassword(config.getPassword());
        this.setInsecureSkipTlsVerify(true);
    }

    public ConfigurationProperties(ChartConfigurationProperties chartConfigurationProperties) {
        this.setBaseUrl(chartConfigurationProperties.getBaseUrl());
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
