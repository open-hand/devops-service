package io.choerodon.devops.api.vo.iam;

import io.swagger.annotations.ApiModelProperty;

/**
 * 不可变的项目的信息
 *
 * @author zmf
 * @since 2021/4/19
 */
public class ImmutableProjectInfoVO {
    @ApiModelProperty("项目code")
    private String projCode;
    @ApiModelProperty("组织id")
    private Long tenantId;
    @ApiModelProperty("组织code")
    private String tenantNum;

    public ImmutableProjectInfoVO() {
    }

    public ImmutableProjectInfoVO(String projCode, Long tenantId, String tenantNum) {
        this.projCode = projCode;
        this.tenantId = tenantId;
        this.tenantNum = tenantNum;
    }

    public String getProjCode() {
        return projCode;
    }

    public void setProjCode(String projCode) {
        this.projCode = projCode;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantNum() {
        return tenantNum;
    }

    public void setTenantNum(String tenantNum) {
        this.tenantNum = tenantNum;
    }
}
