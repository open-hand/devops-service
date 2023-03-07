package io.choerodon.devops.api.vo.pipeline;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.api.vo.DevopsDeployGroupAppConfigVO;
import io.choerodon.devops.api.vo.DevopsDeployGroupContainerConfigVO;

/**
 * @author hao.wang@zknow.com
 * @since 2022-11-07 10:18:26
 */
public class CiDeployDeployCfgVO extends AppDeployConfigVO {
    @ApiModelProperty("部署组：应用配置")
    private DevopsDeployGroupAppConfigVO appConfig;
    @ApiModelProperty("部署组：容器配置")
    private List<DevopsDeployGroupContainerConfigVO> containerConfig;
    @ApiModelProperty(value = "应用配置", required = true)
    private String appConfigJson;
    @ApiModelProperty(value = "容器配置", required = true)
    private String containerConfigJson;

    public DevopsDeployGroupAppConfigVO getAppConfig() {
        return appConfig;
    }

    public void setAppConfig(DevopsDeployGroupAppConfigVO appConfig) {
        this.appConfig = appConfig;
    }

    public List<DevopsDeployGroupContainerConfigVO> getContainerConfig() {
        return containerConfig;
    }

    public void setContainerConfig(List<DevopsDeployGroupContainerConfigVO> containerConfig) {
        this.containerConfig = containerConfig;
    }

    public String getAppConfigJson() {
        return appConfigJson;
    }

    public void setAppConfigJson(String appConfigJson) {
        this.appConfigJson = appConfigJson;
    }

    public String getContainerConfigJson() {
        return containerConfigJson;
    }

    public void setContainerConfigJson(String containerConfigJson) {
        this.containerConfigJson = containerConfigJson;
    }
}
