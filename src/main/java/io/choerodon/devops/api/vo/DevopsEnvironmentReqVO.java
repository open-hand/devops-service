package io.choerodon.devops.api.vo;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author zmf
 */
public class DevopsEnvironmentReqVO {
    @ApiModelProperty("环境名称 / 必需")
    @NotNull(message = "error.name.null")
    private String name;

    @ApiModelProperty("集群ID / 必需")
    @NotNull(message = "error.cluster.id.null")
    private Long clusterId;

    @ApiModelProperty("环境code / 必需")
    @NotNull(message = "error.code.null")
    private String code;

    @ApiModelProperty("环境描述 / 非必需")
    private String description;

    @ApiModelProperty("环境分组ID / 非必需")
    private Long devopsEnvGroupId;

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

    public Long getDevopsEnvGroupId() {
        return devopsEnvGroupId;
    }

    public void setDevopsEnvGroupId(Long devopsEnvGroupId) {
        this.devopsEnvGroupId = devopsEnvGroupId;
    }
}
