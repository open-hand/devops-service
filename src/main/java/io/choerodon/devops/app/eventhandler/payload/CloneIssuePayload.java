package io.choerodon.devops.app.eventhandler.payload;

import java.util.Map;

import io.swagger.annotations.ApiModelProperty;

public class CloneIssuePayload {
    @ApiModelProperty("项目id")
    private Long projectId;
    @ApiModelProperty("key:newIssue value:oldIssueId")
    private Map<Long, Long> newIssueIdMap;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Map<Long, Long> getNewIssueIdMap() {
        return newIssueIdMap;
    }

    public void setNewIssueIdMap(Map<Long, Long> newIssueIdMap) {
        this.newIssueIdMap = newIssueIdMap;
    }
}
