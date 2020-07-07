package io.choerodon.devops.infra.dto.repo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * choerodon nexus服务DTO
 *
 * @author weisen.yang@hand-china.com 2020/7/2
 */
@ApiModel("nexus服务DTO")
public class C7nNexusServerDTO {

    @ApiModelProperty(value = "主键")
    private Long configId;
    @ApiModelProperty(value = "服务名称")
    private String serverName;
    @ApiModelProperty(value = "访问地址")
    private String serverUrl;
    @ApiModelProperty(value = "项目Id")
    private Long projectId;

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    @Override
    public String toString() {
        return "C7nNexusServerDTO{" +
                "configId=" + configId +
                ", serverName='" + serverName + '\'' +
                ", serverUrl='" + serverUrl + '\'' +
                ", projectId=" + projectId +
                '}';
    }
}
