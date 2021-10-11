package io.choerodon.devops.api.vo;

/**
 * Created by wangxiang on 2021/9/27
 * 外部的组织 包括Saas组织，注册组织
 */
public class ExternalTenantVO {
    private Long tenantId;
    private String tenantName;
    private String saasLevel;
    private Boolean register;

    public Boolean getRegister() {
        return register;
    }

    public void setRegister(Boolean register) {
        this.register = register;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getSaasLevel() {
        return saasLevel;
    }

    public void setSaasLevel(String saasLevel) {
        this.saasLevel = saasLevel;
    }
}
