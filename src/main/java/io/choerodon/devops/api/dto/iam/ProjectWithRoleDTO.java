package io.choerodon.devops.api.dto.iam;

import java.util.List;

public class ProjectWithRoleDTO {

    private String name;

    private List<RoleDTO> roles;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<RoleDTO> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleDTO> roles) {
        this.roles = roles;
    }
}
