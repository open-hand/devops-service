package io.choerodon.devops.api.vo;


import java.util.List;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author zmf
 */
public class DevopsEnvironmentViewVO {
    @ApiModelProperty("环境id")
    private Long id;

    @ApiModelProperty("环境名称")
    private String name;

    @ApiModelProperty("环境是否连接")
    private Boolean connect;

    @ApiModelProperty("关联的应用服务")
    private List<DevopsAppServiceViewVO> apps;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DevopsAppServiceViewVO> getApps() {
        return apps;
    }

    public void setApps(List<DevopsAppServiceViewVO> apps) {
        this.apps = apps;
    }

    public Boolean getConnect() {
        return connect;
    }

    public void setConnect(Boolean connect) {
        this.connect = connect;
    }
}
