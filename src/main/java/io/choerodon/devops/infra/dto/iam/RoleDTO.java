package io.choerodon.devops.infra.dto.iam;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author scp
 * @since 2020-01-10
 */
public class RoleDTO {

    @ApiModelProperty("角色id")
    private Long id;
    @ApiModelProperty("角色名")
    private String name;
    @ApiModelProperty("角色编码")
    private String code;

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
}
