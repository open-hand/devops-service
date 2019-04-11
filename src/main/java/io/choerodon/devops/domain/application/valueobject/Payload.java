package io.choerodon.devops.domain.application.valueobject;

import java.util.List;

/**
 * Created by younger on 2018/4/18.
 */
public class Payload {
    private String namespace;
    private String repoUrl;
    private String chartName;
    private List<ImagePullSecret> imagePullSecrets;
    private String chartVersion;
    private String values;
    private String releaseName;

    /**
     * 构造函数
     *
     * @param namespace    命名空间
     * @param repoUrl      仓库地址
     * @param chartName    chart名
     * @param chartVersion chart版本
     * @param values       部署值
     * @param releaseName  release名
     */
    public Payload(String namespace, String repoUrl, String chartName, String chartVersion, String values, String releaseName, List<ImagePullSecret> imagePullSecrets) {
        this.namespace = namespace;
        this.repoUrl = repoUrl;
        this.chartName = chartName;
        this.chartVersion = chartVersion;
        this.values = values;
        this.releaseName = releaseName;
        this.imagePullSecrets = imagePullSecrets;
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

    public List<ImagePullSecret> getImagePullSecrets() {
        return imagePullSecrets;
    }

    public void setImagePullSecrets(List<ImagePullSecret> imagePullSecrets) {
        this.imagePullSecrets = imagePullSecrets;
    }
}
