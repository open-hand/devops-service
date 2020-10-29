package io.choerodon.devops.api.vo;

import java.util.List;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

public class DevopsClusterReqVO {
    @ApiModelProperty("集群名称 / 必需")
    @NotNull(message = "error.name.null")
    private String name;

    @ApiModelProperty("集群编码 / 必需")
    @NotNull(message = "error.code.null")
    private String code;

    @ApiModelProperty("集群描述 / 非必需")
    private String description;

    @ApiModelProperty("集群类型 / 必须")
    private String type;

    @ApiModelProperty("集群节点")
    private List<DevopsClusterNodeVO> devopsClusterInnerNodeVOList;

    @ApiModelProperty("提供外网访问的节点 / 非必须")
    private DevopsClusterNodeVO devopsClusterOutterNodeVO;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<DevopsClusterNodeVO> getDevopsClusterInnerNodeVOList() {
        return devopsClusterInnerNodeVOList;
    }

    public void setDevopsClusterInnerNodeVOList(List<DevopsClusterNodeVO> devopsClusterInnerNodeVOList) {
        this.devopsClusterInnerNodeVOList = devopsClusterInnerNodeVOList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DevopsClusterNodeVO getDevopsClusterOutterNodeVO() {
        return devopsClusterOutterNodeVO;
    }

    public DevopsClusterReqVO setDevopsClusterOutterNodeVO(DevopsClusterNodeVO devopsClusterOutterNodeVO) {
        this.devopsClusterOutterNodeVO = devopsClusterOutterNodeVO;
        return this;
    }
}
