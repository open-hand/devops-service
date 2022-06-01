package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * Created by Sheep on 2019/7/2.
 */
public class DevopsCustomizeResourceVO extends DevopsResourceDataInfoVO{
    @Encrypt
    private Long id;
    @ApiModelProperty("项目id")
    private Long projectId;
    @Encrypt
    @ApiModelProperty(value = "环境id")
    private Long envId;
    @Encrypt
    @ApiModelProperty(value = "集群id")
    private Long clusterId;
    @ApiModelProperty(value = "环境状态")
    private Boolean envStatus;
    @ApiModelProperty(value = "环境编码")
    private String envCode;
    @ApiModelProperty(value = "资源内容")
    private String resourceContent;
    @ApiModelProperty(value = "资源类型")
    private String k8sKind;
    @ApiModelProperty(value = "commandStatus")
    private String commandStatus;
    @ApiModelProperty(value = "commandType")
    private String commandType;
    @ApiModelProperty(value = "错误信息")
    private String error;
    @ApiModelProperty(value = "名称")
    private String name;
    @ApiModelProperty(value = "描述")
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Boolean getEnvStatus() {
        return envStatus;
    }

    public void setEnvStatus(Boolean envStatus) {
        this.envStatus = envStatus;
    }

    public String getResourceContent() {
        return resourceContent;
    }

    public void setResourceContent(String resourceContent) {
        this.resourceContent = resourceContent;
    }

    public String getK8sKind() {
        return k8sKind;
    }

    public void setK8sKind(String k8sKind) {
        this.k8sKind = k8sKind;
    }

    public String getCommandStatus() {
        return commandStatus;
    }

    public void setCommandStatus(String commandStatus) {
        this.commandStatus = commandStatus;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    public String getEnvCode() {
        return envCode;
    }

    public void setEnvCode(String envCode) {
        this.envCode = envCode;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }
}
