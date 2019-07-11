package io.choerodon.devops.api.vo.iam;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

/**
 * Created by n!Ck
 * Date: 2018/10/26
 * Time: 13:20
 * Description:
 */
public class UserWithRoleDTO extends UserVO {
    @ApiModelProperty(value = "角色列表")
    private List<RoleDTO> roles;

    public List<RoleDTO> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleDTO> roles) {
        this.roles = roles;
    }
}
