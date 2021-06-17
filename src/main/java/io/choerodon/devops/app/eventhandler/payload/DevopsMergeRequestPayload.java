package io.choerodon.devops.app.eventhandler.payload;

import java.util.List;

public class DevopsMergeRequestPayload {
    private String serviceCode;
    private List<Long> issueIds;
    private Long projectId;

    public List<Long> getIssueIds() {
        return issueIds;
    }

    public void setIssueIds(List<Long> issueIds) {
        this.issueIds = issueIds;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
