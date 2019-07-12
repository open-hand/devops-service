package io.choerodon.devops.infra.dto.iam;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;


/**
 * Created by Zenger on 2018/3/28.
 */
public class ProjectDTO {

    private Long id;
    private String name;
    private Long organizationId;
    private String code;

    public ProjectDTO() {

    }

    public ProjectDTO(Long id) {
        this.id = id;
    }

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

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
