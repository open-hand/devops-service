package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  17:19 2019/8/5
 * Description:
 */
public class ApplicationImportInternalVO {
    @Encrypt
    @ApiModelProperty("应用服务id")
    private Long appServiceId;
    @Encrypt
    @ApiParam("版本id")
    private Long versionId;
    @ApiParam("应用服务类型")
    private String type;
    @ApiParam("应用名称")
    private String appName;
    @ApiParam("应用编码")
    private String appCode;
    @Encrypt
    @ApiParam("应用市场部署对象id")
    private Long deployObjectId;

    public Long getDeployObjectId() {
        return deployObjectId;
    }

    public void setDeployObjectId(Long deployObjectId) {
        this.deployObjectId = deployObjectId;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }
}
