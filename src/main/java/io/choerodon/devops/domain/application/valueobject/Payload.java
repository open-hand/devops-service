package io.choerodon.devops.domain.application.valueobject;

/**
 * Created by younger on 2018/4/18.
 */
public class Payload {
    private String namespace;
    private String repoUrl;
    private String chartName;
    private String chartVersion;
    private String values;
    private String releaseName;

    /**
     * 构造函数
     */
    public Payload(String namespace, String repoUrl, String chartName, String chartVersion, String values, String releaseName) {
        this.namespace = namespace;
        this.repoUrl = repoUrl;
        this.chartName = chartName;
        this.chartVersion = chartVersion;
        this.values = values;
        this.releaseName = releaseName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public String getChartName() {
        return chartName;
    }

    public void setChartName(String chartName) {
        this.chartName = chartName;
    }

    public String getChartVersion() {
        return chartVersion;
    }

    public void setChartVersion(String chartVersion) {
        this.chartVersion = chartVersion;
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public void setReleaseName(String releaseName) {
        this.releaseName = releaseName;
    }
}
