package io.choerodon.devops.api.vo.dashboard;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/6/12 16:10
 */
public class ProjectMeasureVO {
    private double codeScore;
    private double vulnScore;
    private double k8sScore;

    public double getCodeScore() {
        return codeScore;
    }

    public void setCodeScore(double codeScore) {
        this.codeScore = codeScore;
    }

    public double getVulnScore() {
        return vulnScore;
    }

    public void setVulnScore(double vulnScore) {
        this.vulnScore = vulnScore;
    }

    public double getK8sScore() {
        return k8sScore;
    }

    public void setK8sScore(double k8sScore) {
        this.k8sScore = k8sScore;
    }
}
