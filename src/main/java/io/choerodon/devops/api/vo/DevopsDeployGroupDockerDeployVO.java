package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.vo.harbor.ProdImageInfoVO;

public class DevopsDeployGroupDockerDeployVO {
    @ApiModelProperty("容器名")
    private String name;
    @ApiModelProperty("部署来源")
    private String sourceType;
    @ApiModelProperty("制品库镜像信息")
    private ProdImageInfoVO imageInfo;
    @ApiModelProperty("应用市场部署对象id")
    @Encrypt
    private Long deployObjectId;
    @ApiModelProperty("应用市场应用版本id")
    @Encrypt
    private Long mktAppVersionId;
    @Encrypt
    private Long appServiceVersionId;

    @Encrypt
    private Long appServiceId;

    public Long getAppServiceVersionId() {
        return appServiceVersionId;
    }

    public void setAppServiceVersionId(Long appServiceVersionId) {
        this.appServiceVersionId = appServiceVersionId;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public ProdImageInfoVO getImageInfo() {
        return imageInfo;
    }

    public void setImageInfo(ProdImageInfoVO imageInfo) {
        this.imageInfo = imageInfo;
    }

    public Long getDeployObjectId() {
        return deployObjectId;
    }

    public void setDeployObjectId(Long deployObjectId) {
        this.deployObjectId = deployObjectId;
    }

    public Long getMktAppVersionId() {
        return mktAppVersionId;
    }

    public void setMktAppVersionId(Long mktAppVersionId) {
        this.mktAppVersionId = mktAppVersionId;
    }
}
