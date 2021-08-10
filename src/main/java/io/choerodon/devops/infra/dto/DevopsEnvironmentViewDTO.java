package io.choerodon.devops.infra.dto;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @author zmf
 */
public class DevopsEnvironmentViewDTO {
    @ApiModelProperty("环境id")
    private Long id;
    @ApiModelProperty("环境名称")
    private String name;
    @ApiModelProperty("集群id")
    private Long clusterId;
    @ApiModelProperty("环境code")
    private String code;
    private List<DevopsApplicationViewDTO> apps;

    @ApiModelProperty("实例id")
    private Long instanceId;

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

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

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    public List<DevopsApplicationViewDTO> getApps() {
        return apps;
    }

    public void setApps(List<DevopsApplicationViewDTO> apps) {
        this.apps = apps;
    }

    public String getCode() {
        return code;
    }

    public DevopsEnvironmentViewDTO setCode(String code) {
        this.code = code;
        return this;
    }
}
