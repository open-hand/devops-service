package io.choerodon.devops.api.vo.deploy;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.vo.harbor.ExternalImageInfo;
import io.choerodon.devops.api.vo.harbor.ProdImageInfoVO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/6/30 14:29
 */
public class DockerDeployVO {
    @Encrypt
    @ApiModelProperty("主机id")
    private Long hostId;
    @ApiModelProperty("容器名")
    private String containerName;
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

    @ApiModelProperty("部署values")
    @NotNull(message = "{devops.value.is.null}")
    private String value;

    @ApiModelProperty("主机应用的名称")
    private String appName;
    @ApiModelProperty("修改得时候传的是name")
    private String name;

    @ApiModelProperty("主机应用的code")
    private String appCode;

    @ApiModelProperty("仓库类型(自定义的还是默认的)")
    private String repoType;
    @ApiModelProperty("操作类型 create/update")
    private String operation;

    @ApiModelProperty("hostAppId")
    @Encrypt
    private Long hostAppId;

    @ApiModelProperty("外部自定义仓库信息")
    private ExternalImageInfo externalImageInfo;

    public Long getHostId() {
        return hostId;
    }

    public DockerDeployVO setHostId(Long hostId) {
        this.hostId = hostId;
        return this;
    }

    public String getContainerName() {
        return containerName;
    }

    public DockerDeployVO setContainerName(String containerName) {
        this.containerName = containerName;
        return this;
    }

    public String getSourceType() {
        return sourceType;
    }

    public DockerDeployVO setSourceType(String sourceType) {
        this.sourceType = sourceType;
        return this;
    }

    public ProdImageInfoVO getImageInfo() {
        return imageInfo;
    }

    public DockerDeployVO setImageInfo(ProdImageInfoVO imageInfo) {
        this.imageInfo = imageInfo;
        return this;
    }

    public Long getDeployObjectId() {
        return deployObjectId;
    }

    public DockerDeployVO setDeployObjectId(Long deployObjectId) {
        this.deployObjectId = deployObjectId;
        return this;
    }

    public Long getMktAppVersionId() {
        return mktAppVersionId;
    }

    public DockerDeployVO setMktAppVersionId(Long mktAppVersionId) {
        this.mktAppVersionId = mktAppVersionId;
        return this;
    }

    public String getValue() {
        return value;
    }

    public DockerDeployVO setValue(String value) {
        this.value = value;
        return this;
    }

    public String getAppName() {
        return appName;
    }

    public DockerDeployVO setAppName(String appName) {
        this.appName = appName;
        return this;
    }

    public String getName() {
        return name;
    }

    public DockerDeployVO setName(String name) {
        this.name = name;
        return this;
    }

    public String getAppCode() {
        return appCode;
    }

    public DockerDeployVO setAppCode(String appCode) {
        this.appCode = appCode;
        return this;
    }

    public String getRepoType() {
        return repoType;
    }

    public DockerDeployVO setRepoType(String repoType) {
        this.repoType = repoType;
        return this;
    }

    public String getOperation() {
        return operation;
    }

    public DockerDeployVO setOperation(String operation) {
        this.operation = operation;
        return this;
    }

    public Long getHostAppId() {
        return hostAppId;
    }

    public DockerDeployVO setHostAppId(Long hostAppId) {
        this.hostAppId = hostAppId;
        return this;
    }

    public ExternalImageInfo getExternalImageInfo() {
        return externalImageInfo;
    }

    public DockerDeployVO setExternalImageInfo(ExternalImageInfo externalImageInfo) {
        this.externalImageInfo = externalImageInfo;
        return this;
    }
}
