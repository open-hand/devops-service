package io.choerodon.devops.domain.application.event;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  15:57 2019/7/5
 * Description:
 */
public class DevOpsAppDelPayload {
    private Long organizationId;
    private Long projectId;
    private String code;

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
