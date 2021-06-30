package io.choerodon.devops.api.vo.deploy;

import io.choerodon.devops.api.vo.harbor.ProdImageInfoVO;
import io.swagger.annotations.ApiModelProperty;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/6/30 14:29
 */
public class DockerDeployVO {
    @ApiModelProperty("主机id")
    private Long hostId;
    @ApiModelProperty("容器名")
    private String name;
    @ApiModelProperty("主机端口")
    private Integer hostPort;
    @ApiModelProperty("容器端口")
    private Integer containerPort;
    @ApiModelProperty("部署来源")
    private String sourceType;
    @ApiModelProperty("镜像信息")
    private ProdImageInfoVO imageInfo;

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

    public Integer getHostPort() {
        return hostPort;
    }

    public void setHostPort(Integer hostPort) {
        this.hostPort = hostPort;
    }

    public Integer getContainerPort() {
        return containerPort;
    }

    public void setContainerPort(Integer containerPort) {
        this.containerPort = containerPort;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }
}
