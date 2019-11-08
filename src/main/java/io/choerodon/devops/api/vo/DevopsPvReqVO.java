package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;


public class DevopsPvReqVO {

    @ApiModelProperty("pvId")
    private Long id;

    @ApiModelProperty("pv名称")
    @Pattern(regexp = "[a-z]([-a-z0-9]*[a-z0-9])?(\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*", message = "error.pv.name.pattern")
    @Length(max = 40, min = 1, message = "error.pv.name.length")
    private String name;

    @NotNull(message = "error.pv.type.is.null")
    @ApiModelProperty("pv类型")
    private String type;

    @ApiModelProperty("pv描述")
    private String description;

    @NotNull(message = "error.pv.related.cluster.is.null")
    @ApiModelProperty("关联的集群Id")
    private Long clusterId;

    @NotNull(message = "error.pv.accessmodes.is.null")
    @ApiModelProperty("访问模式")
    private String accessModes;

    @NotNull(message = "error.pv.checkpermission.is.null")
    @ApiModelProperty("是否跳过权限校验，默认为true")
    private Boolean skipCheckProjectPermission;

    @NotNull(message = "error.pv.requestResource.is.null")
    @ApiModelProperty("资源大小")
    private String requestResource;

    @ApiModelProperty(value = "创建还是更新", hidden = true)
    private String commandType;

    @ApiModelProperty(value = "环境id", hidden = true)
    private Long envId;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    public String getAccessModes() {
        return accessModes;
    }

    public void setAccessModes(String accessModes) {
        this.accessModes = accessModes;
    }

    public Boolean getSkipCheckProjectPermission() {
        return skipCheckProjectPermission;
    }

    public void setSkipCheckProjectPermission(Boolean skipCheckProjectPermission) {
        this.skipCheckProjectPermission = skipCheckProjectPermission;
    }

    public String getRequestResource() {
        return requestResource;
    }

    public void setRequestResource(String requestResource) {
        this.requestResource = requestResource;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }
}
