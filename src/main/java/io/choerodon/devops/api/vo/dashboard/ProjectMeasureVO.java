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
    private String codeScore;
    private String vulnScore;
    private String k8sScore;

    private String score;

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

    public String getCodeScore() {
        return codeScore;
    }

    public void setCodeScore(String codeScore) {
        this.codeScore = codeScore;
    }

    public String getVulnScore() {
        return vulnScore;
    }

    public void setVulnScore(String vulnScore) {
        this.vulnScore = vulnScore;
    }

    public String getK8sScore() {
        return k8sScore;
    }

    public void setK8sScore(String k8sScore) {
        this.k8sScore = k8sScore;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }
}
