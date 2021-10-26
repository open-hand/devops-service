package io.choerodon.devops.infra.dto.governance;

import io.swagger.annotations.ApiModelProperty;

import java.util.Set;

public class NacosListenConfigDTO {
    @ApiModelProperty(value = "配置Id")
    private Long configId;
    @ApiModelProperty(value = "nacos服务地址")
    private String nacosServerAddr;
    @ApiModelProperty(value = "命名空间Id")
    private String namespaceId;
    @ApiModelProperty(value = "分组")
    private String group;
    @ApiModelProperty(value = "dataId")
    private String dataId;
    @ApiModelProperty(value = "文件挂载路径")
    private Set<String> mountPaths;
    @ApiModelProperty(value = "实例名")
    private String instanceName;

    public Long getConfigId() {
        return configId;
    }

    public NacosListenConfigDTO setConfigId(Long configId) {
        this.configId = configId;
        return this;
    }

    public String getNacosServerAddr() {
        return nacosServerAddr;
    }

    public NacosListenConfigDTO setNacosServerAddr(String nacosServerAddr) {
        this.nacosServerAddr = nacosServerAddr;
        return this;
    }

    public String getNamespaceId() {
        return namespaceId;
    }

    public NacosListenConfigDTO setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public NacosListenConfigDTO setGroup(String group) {
        this.group = group;
        return this;
    }

    public String getDataId() {
        return dataId;
    }

    public NacosListenConfigDTO setDataId(String dataId) {
        this.dataId = dataId;
        return this;
    }

    public Set<String> getMountPaths() {
        return mountPaths;
    }

    public void setMountPaths(Set<String> mountPaths) {
        this.mountPaths = mountPaths;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }
}
