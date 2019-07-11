package io.choerodon.devops.api.vo.iam;

import java.util.List;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * Created by n!Ck
 * Date: 2018/10/26
 * Time: 13:21
 * Description:
 */
public class RoleDTO {
    @ApiModelProperty(value = "主键ID/非必填")
    private Long id;

    @ApiModelProperty(value = "角色名/必填")
    @NotEmpty(message = "error.role.name.empty")
    @Size(min = 1, max = 64)
    private String name;

    @ApiModelProperty(value = "角色编码/必填")
    @NotEmpty(message = "error.role.code.empty")
    @Size(min = 1, max = 128)
    private String code;

    @ApiModelProperty(value = "角色描述/非必填")
    private String description;

    @ApiModelProperty(value = "角色层级/必填")
    @NotEmpty(message = "error.role.level.empty")
    private String level;

    @ApiModelProperty(value = "是否启用/非必填")
    private Boolean enabled;
    @ApiModelProperty(value = "是否允许修改/非必填")
    private Boolean modified;
    @ApiModelProperty(value = "是否允许禁用/非必填")
    private Boolean enableForbidden;
    @ApiModelProperty(value = "是否内置角色/非必填")
    private Boolean builtIn;
    @ApiModelProperty(value = "是否匀巡被分配/非必填")
    private Boolean assignable;
    @ApiModelProperty(value = "乐观锁版本号")
    private Long objectVersionNumber;
    @ApiModelProperty(value = "已分配用户数量/非必填")
    private Integer userCount;
    @ApiModelProperty(value = "权限列表/非必填")
    private List<PermissionDTO> permissions;
    @ApiModelProperty(value = "角色权限列表/非必填")
    private List<RolePermissionDTO> rolePermissions;
    @ApiModelProperty(value = "角色标签列表/非必填")
    private List<LabelDTO> labels;
    @ApiModelProperty(value = "分配用户列表/非必填")
    private List<UserDTO> users;
    @ApiModelProperty(value = "组织名/非必填")
    private String organizationName;
    @ApiModelProperty(value = "项目名/非必填")
    private String projectName;

    @JsonIgnore
    private String param;

    private List<Long> roleIds;

    public List<UserDTO> getUsers() {
        return users;
    }

    public void setUsers(List<UserDTO> users) {
        this.users = users;
    }

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }

    public List<RolePermissionDTO> getRolePermissions() {
        return rolePermissions;
    }

    public void setRolePermissions(List<RolePermissionDTO> rolePermissions) {
        this.rolePermissions = rolePermissions;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getModified() {
        return modified;
    }

    public void setModified(Boolean modified) {
        this.modified = modified;
    }

    public Boolean getEnableForbidden() {
        return enableForbidden;
    }

    public void setEnableForbidden(Boolean enableForbidden) {
        this.enableForbidden = enableForbidden;
    }

    public Boolean getBuiltIn() {
        return builtIn;
    }

    public void setBuiltIn(Boolean builtIn) {
        this.builtIn = builtIn;
    }

    public Boolean getAssignable() {
        return assignable;
    }

    public void setAssignable(Boolean assignable) {
        this.assignable = assignable;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public List<PermissionDTO> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PermissionDTO> permissions) {
        this.permissions = permissions;
    }

    public Integer getUserCount() {
        return userCount;
    }

    public void setUserCount(Integer userCount) {
        this.userCount = userCount;
    }

    public List<LabelDTO> getLabels() {
        return labels;
    }

    public void setLabels(List<LabelDTO> labels) {
        this.labels = labels;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
