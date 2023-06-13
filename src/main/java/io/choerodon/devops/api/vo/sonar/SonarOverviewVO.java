package io.choerodon.devops.api.vo.sonar;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/6/13 14:36
 */
public class SonarOverviewVO {
    private Long bugs;
    private Long vulnerabilities;
    private Long codeSmells;
    private String debt;
    private Long duplicatedLines;

    public Long getBugs() {
        return bugs;
    }

    public void setBugs(Long bugs) {
        this.bugs = bugs;
    }

    public Long getVulnerabilities() {
        return vulnerabilities;
    }

    public void setVulnerabilities(Long vulnerabilities) {
        this.vulnerabilities = vulnerabilities;
    }

    public Long getCodeSmells() {
        return codeSmells;
    }

    public void setCodeSmells(Long codeSmells) {
        this.codeSmells = codeSmells;
    }

    public String getDebt() {
        return debt;
    }

    public void setDebt(String debt) {
        this.debt = debt;
    }

    public Long getDuplicatedLines() {
        return duplicatedLines;
    }

    public void setDuplicatedLines(Long duplicatedLines) {
        this.duplicatedLines = duplicatedLines;
    }
}
