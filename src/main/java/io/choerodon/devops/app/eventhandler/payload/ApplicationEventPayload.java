package io.choerodon.devops.app.eventhandler.payload;

import java.util.Set;

/**
 * @author zongw.lee@gmail.com
 * @since 2019/8/7
 */
public class ApplicationEventPayload {
    private Long id;

    private String name;

    private String code;

    private Long organizationId;

    private String imageUrl;

    private String type;

    private Long sourceId;

    private String organizationCode;

    private String organizationName;

    private Long projectId;

    private Long userId;

    /**
     * 如果这个值不为空，则当前应用选择某个特定版本的平台层应用作为模板，这是该特定版本每个服务的版本ID，对应`devops_app_service_version`表
     */
    private Set<Long> serviceVersionIds;

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

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public String getOrganizationCode() {
        return organizationCode;
    }

    public void setOrganizationCode(String organizationCode) {
        this.organizationCode = organizationCode;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Set<Long> getServiceVersionIds() {
        return serviceVersionIds;
    }

    public void setServiceVersionIds(Set<Long> serviceVersionIds) {
        this.serviceVersionIds = serviceVersionIds;
    }
}
