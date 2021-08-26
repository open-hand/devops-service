package io.choerodon.devops.api.vo.deploy;

import io.choerodon.devops.api.vo.harbor.ProdImageInfoVO;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import javax.validation.constraints.NotNull;

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
    private String name;
    @ApiModelProperty("部署来源")
    private String sourceType;
    @ApiModelProperty("制品库镜像信息")
    private ProdImageInfoVO imageInfo;
    /**
     *  应用市场部署对象id
     */
    @Encrypt
    private Long deployObjectId;

    @ApiModelProperty("部署values")
    @NotNull(message = "error.value.is.null")
    private String value;

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

    public ProdImageInfoVO getImageInfo() {
        return imageInfo;
    }

    public void setImageInfo(ProdImageInfoVO imageInfo) {
        this.imageInfo = imageInfo;
    }

    public Long getHostId() {
        return hostId;
    }

    public void setHostId(Long hostId) {
        this.hostId = hostId;
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
}
