package io.choerodon.devops.api.vo;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

/**
 * 集群的环境详情接口需要的数据
 *
 * @author zmf
 * @since 2/25/20
 */
public class ClusterPolarisEnvDetailsVO {
    @ApiModelProperty("猪齿鱼平台内部环境")
    private List<DevopsEnvWithPolarisResultVO> internal;
    @ApiModelProperty("存在集群中的外部环境")
    private List<DevopsEnvWithPolarisResultVO> external;

    public ClusterPolarisEnvDetailsVO() {
    }

    public ClusterPolarisEnvDetailsVO(List<DevopsEnvWithPolarisResultVO> internal, List<DevopsEnvWithPolarisResultVO> external) {
        this.internal = internal;
        this.external = external;
    }

    public List<DevopsEnvWithPolarisResultVO> getInternal() {
        return internal;
    }

    public void setInternal(List<DevopsEnvWithPolarisResultVO> internal) {
        this.internal = internal;
    }

    public List<DevopsEnvWithPolarisResultVO> getExternal() {
        return external;
    }

    public void setExternal(List<DevopsEnvWithPolarisResultVO> external) {
        this.external = external;
    }
}
