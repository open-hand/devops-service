package io.choerodon.devops.api.dto;

import java.util.List;

/**
 * Created by younger on 2018/3/28.
 */
public class ApplicationDTO {

    private Long id;
    private String name;
    private String code;
    private Long projectId;
    private String type;
    private Long applictionTemplateId;
    private List<Long> userIds;
    private Boolean permission;

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

    public Long getApplictionTemplateId() {
        return applictionTemplateId;
    }

    public void setApplictionTemplateId(Long applictionTemplateId) {
        this.applictionTemplateId = applictionTemplateId;
    }

    public List<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }

    public Boolean getPermission() {
        return permission;
    }

    public void setPermission(Boolean permission) {
        this.permission = permission;
    }
}
