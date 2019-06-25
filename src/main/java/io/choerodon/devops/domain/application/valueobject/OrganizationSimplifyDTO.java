package io.choerodon.devops.domain.application.valueobject;

import io.swagger.annotations.ApiModelProperty;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  17:44 2019/6/24
 * Description:
 */
public class OrganizationSimplifyDTO {
    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "组织名")
    private String name;

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
}
