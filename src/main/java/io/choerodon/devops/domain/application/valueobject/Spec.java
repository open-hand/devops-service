package io.choerodon.devops.domain.application.valueobject;

import java.util.List;

public class Spec {


    private String repoUrl;
    private String chartName;
    private List<ImagePullSecret> imagePullSecrets;
    private String chartVersion;
    private String values;

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

    public List<ImagePullSecret> getImagePullSecrets() {
        return imagePullSecrets;
    }

    public void setImagePullSecrets(List<ImagePullSecret> imagePullSecrets) {
        this.imagePullSecrets = imagePullSecrets;
    }
}
