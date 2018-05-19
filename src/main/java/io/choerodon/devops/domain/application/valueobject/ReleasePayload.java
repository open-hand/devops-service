package io.choerodon.devops.domain.application.valueobject;


public class ReleasePayload {

    private String name;
    private Long revision;
    private String namespace;
    private String status;
    private String chartName;
    private String chartVersion;
    private String config;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getRevision() {
        return revision;
    }

    public void setRevision(Long revision) {
        this.revision = revision;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }
}
