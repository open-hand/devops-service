package io.choerodon.devops.api.dto;

/**
 * Created by younger on 2018/3/27.
 */
public class ApplicationTemplateDTO {

    private Long id;
    private Long organizationId;
    private String name;
    private String description;
    private String code;
    private Long copyFrom;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getCopyFrom() {
        return copyFrom;
    }

    public void setCopyFrom(Long copyFrom) {
        this.copyFrom = copyFrom;
    }


}
