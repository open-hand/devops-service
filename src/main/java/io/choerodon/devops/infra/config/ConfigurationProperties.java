package io.choerodon.devops.infra.config;

import io.choerodon.devops.api.vo.ProjectConfigDTO;

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

    public ConfigurationProperties(ProjectConfigDTO config) {
        this.setBaseUrl(config.getUrl());
        this.setProject(config.getProject());
        this.setUsername(config.getUserName());
        this.setPassword(config.getPassword());
        //暂时设置为 false => 去校验用户
        this.setInsecureSkipTlsVerify(false);
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
