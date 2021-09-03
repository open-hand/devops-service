package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * @Author: scp
 * @Description:
 * @Date: Created in 2021/9/2
 * @Modified By:
 */
public class CheckAppServiceCodeAndNameVO {
    private String serviceCode;
    private String serviceName;
    @ApiModelProperty("true:校验通过，false:已经存在该code，null:传参为null，没有校验")
    private Boolean codeEnabledFlag;
    private Boolean nameEnabledFlag;

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Boolean getCodeEnabledFlag() {
        return codeEnabledFlag;
    }

    public void setCodeEnabledFlag(Boolean codeEnabledFlag) {
        this.codeEnabledFlag = codeEnabledFlag;
    }

    public Boolean getNameEnabledFlag() {
        return nameEnabledFlag;
    }

    public void setNameEnabledFlag(Boolean nameEnabledFlag) {
        this.nameEnabledFlag = nameEnabledFlag;
    }
}
