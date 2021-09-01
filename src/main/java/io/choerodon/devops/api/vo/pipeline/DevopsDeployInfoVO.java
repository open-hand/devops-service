package io.choerodon.devops.api.vo.pipeline;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.vo.DevopsDeployGroupAppConfigVO;
import io.choerodon.devops.api.vo.DevopsDeployGroupContainerConfigVO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/9/1 11:44
 */
public class DevopsDeployInfoVO {
    @Encrypt
    @ApiModelProperty("环境id")
    private Long envId;
    @ApiModelProperty("是否跳过环境权限（是否允许非环境人员触发此任务）")
    private Boolean skipCheckPermission;
    @ApiModelProperty("部署方式，后端查询时设置，实例存在则更新，否则新建")
    private String deployType;
    /**
     * {@link io.choerodon.devops.infra.enums.deploy.RdupmTypeEnum}
     */
    @ApiModelProperty("部署对象")
    private String deployObjectType;
    @ApiModelProperty("应用名称")
    private String name;
    @ApiModelProperty("应用编码")
    private String code;
    @Encrypt
    @ApiModelProperty("chart包: 部署配置id")
    private Long valueId;
    @ApiModelProperty("部署组：应用配置")
    private DevopsDeployGroupAppConfigVO appConfig;
    @ApiModelProperty("部署组：容器配置")
    private List<DevopsDeployGroupContainerConfigVO> containerConfig;

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public Boolean getSkipCheckPermission() {
        return skipCheckPermission;
    }

    public void setSkipCheckPermission(Boolean skipCheckPermission) {
        this.skipCheckPermission = skipCheckPermission;
    }

    public String getDeployType() {
        return deployType;
    }

    public void setDeployType(String deployType) {
        this.deployType = deployType;
    }

    public String getDeployObjectType() {
        return deployObjectType;
    }

    public void setDeployObjectType(String deployObjectType) {
        this.deployObjectType = deployObjectType;
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

    public Long getValueId() {
        return valueId;
    }

    public void setValueId(Long valueId) {
        this.valueId = valueId;
    }

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
}
