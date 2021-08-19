package io.choerodon.devops.api.vo.deploy;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.vo.rdupm.ProdJarInfoVO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/7/1 9:10
 */
public class JarDeployVO {
    @Encrypt
    @ApiModelProperty("主机id")
    private Long hostId;

    @ApiModelProperty("部署来源")
    private String sourceType;

    @ApiModelProperty("应用名")
    private String appName;

    @ApiModelProperty("应用编码")
    private String appCode;

    @ApiModelProperty("实例名")
    private String name;
    /**
     * 部署对象id
     */
    @Encrypt
    private Long deployObjectId;

    @ApiModelProperty("部署values")
    @NotNull(message = "error.value.is.null")
    private String value;

    private ProdJarInfoVO prodJarInfoVO;

    public JarDeployVO() {
        this.value = "";
    }

    public JarDeployVO(String sourceType, String value, ProdJarInfoVO prodJarInfoVO) {
        this.sourceType = sourceType;
        this.value = value;
        this.prodJarInfoVO = prodJarInfoVO;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getDeployObjectId() {
        return deployObjectId;
    }

    public void setDeployObjectId(Long deployObjectId) {
        this.deployObjectId = deployObjectId;
    }

    public ProdJarInfoVO getProdJarInfoVO() {
        return prodJarInfoVO;
    }

    public void setProdJarInfoVO(ProdJarInfoVO prodJarInfoVO) {
        this.prodJarInfoVO = prodJarInfoVO;
    }

    public String getSourceType() {
        return sourceType;
    }

    public Long getHostId() {
        return hostId;
    }

    public void setHostId(Long hostId) {
        this.hostId = hostId;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }


}
