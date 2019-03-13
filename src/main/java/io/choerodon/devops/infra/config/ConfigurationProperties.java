package io.choerodon.devops.infra.config;

public class ConfigurationProperties extends HarborConfigurationProperties {

    private String type;


    public ConfigurationProperties() {

    }

    public ConfigurationProperties(HarborConfigurationProperties harborConfigurationProperties) {
        this.setBaseUrl(harborConfigurationProperties.getBaseUrl());
        this.setProject(harborConfigurationProperties.getProject());
        this.setUsername(harborConfigurationProperties.getUsername());
        this.setPassword(harborConfigurationProperties.getPassword());
        this.setInsecureSkipTlsVerify(harborConfigurationProperties.getInsecureSkipTlsVerify());
        this.setEnabled(harborConfigurationProperties.isEnabled());
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
