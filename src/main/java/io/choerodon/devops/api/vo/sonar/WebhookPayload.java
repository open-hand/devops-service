package io.choerodon.devops.api.vo.sonar;

import java.util.Date;
import java.util.List;
import java.util.Map;

import io.choerodon.devops.infra.dto.SonarAnalyseIssueAuthorDTO;
import io.choerodon.devops.infra.dto.SonarAnalyseIssueSeverityDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/6/6 16:08
 */
public class WebhookPayload {
    private Date analysedAt;
    private Project project;

    private String revision;

    private Map<String, String> properties;

    List<SonarAnalyseIssueSeverityDTO> sonarAnalyseIssueSeverityList;
    private Map<String, SonarAnalyseIssueAuthorDTO> userMap;

    private List<Measure> measures;

    public List<SonarAnalyseIssueSeverityDTO> getSonarAnalyseIssueSeverityList() {
        return sonarAnalyseIssueSeverityList;
    }

    public void setSonarAnalyseIssueSeverityList(List<SonarAnalyseIssueSeverityDTO> sonarAnalyseIssueSeverityList) {
        this.sonarAnalyseIssueSeverityList = sonarAnalyseIssueSeverityList;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public Map<String, SonarAnalyseIssueAuthorDTO> getUserMap() {
        return userMap;
    }

    public void setUserMap(Map<String, SonarAnalyseIssueAuthorDTO> userMap) {
        this.userMap = userMap;
    }

    public List<Measure> getMeasures() {
        return measures;
    }

    public void setMeasures(List<Measure> measures) {
        this.measures = measures;
    }

    public Date getAnalysedAt() {
        return analysedAt;
    }

    public void setAnalysedAt(Date analysedAt) {
        this.analysedAt = analysedAt;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}
