package io.choerodon.devops.infra.config;

import io.swagger.annotations.ApiModelProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 关于CertManager安装的相关配置
 *
 * @author zmf
 * @since 2021/3/25
 */
@Component
@ConfigurationProperties(prefix = "agent.cert-manager")
public class CertManagerProperties {
    @ApiModelProperty("实例名称")
    private String releaseName;
    @ApiModelProperty("CertManager所在的命名空间")
    private String namespace;
    @ApiModelProperty("CertManager的版本")
    private String chartVersion;
    @ApiModelProperty("存放CertManager的chart的仓库地址，用于安装")
    private String repoUrl;

    public String getReleaseName() {
        return releaseName;
    }

    public void setReleaseName(String releaseName) {
        this.releaseName = releaseName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getChartVersion() {
        return chartVersion;
    }

    public void setChartVersion(String chartVersion) {
        this.chartVersion = chartVersion;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }
}
