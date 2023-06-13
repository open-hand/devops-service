package io.choerodon.devops.api.vo.dashboard;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/6/12 16:10
 */
public class ProjectMeasureVO {

    private Long id;
    private String name;
    private Double codeScore;
    private Double vulnScore;
    private Double k8sScore;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getCodeScore() {
        return codeScore;
    }

    public void setCodeScore(Double codeScore) {
        this.codeScore = codeScore;
    }

    public Double getVulnScore() {
        return vulnScore;
    }

    public void setVulnScore(Double vulnScore) {
        this.vulnScore = vulnScore;
    }

    public Double getK8sScore() {
        return k8sScore;
    }

    public void setK8sScore(Double k8sScore) {
        this.k8sScore = k8sScore;
    }
}
