package io.choerodon.devops.api.vo.sonar;

import java.util.Date;
import java.util.Map;

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

    private Map<String, String> properties;

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
