package io.choerodon.devops.api.dto;

import java.util.List;

/**
 * Created by younger on 2018/3/28.
 */
public class ApplicationReqDTO {

    private Long id;
    private String name;
    private String code;
    private Long projectId;
    private String type;
    private Long applicationTemplateId;
    private List<Long> userIds;
    private Boolean isSkipCheckPermission;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getApplicationTemplateId() {
        return applicationTemplateId;
    }

    public void setApplicationTemplateId(Long applicationTemplateId) {
        this.applicationTemplateId = applicationTemplateId;
    }

    public List<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }

    public Boolean getIsSkipCheckPermission() {
        return isSkipCheckPermission;
    }

    public void setIsSkipCheckPermission(Boolean skipCheckPermission) {
        isSkipCheckPermission = skipCheckPermission;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
