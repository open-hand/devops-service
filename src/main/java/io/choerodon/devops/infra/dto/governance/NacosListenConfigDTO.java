package io.choerodon.devops.infra.dto.governance;

import io.swagger.annotations.ApiModelProperty;

import java.util.Map;
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
    @ApiModelProperty(value = "用户名")
    private String userName;
    @ApiModelProperty(value = "密码")
    private String password;

    @ApiModelProperty(value = "文件挂载路径")
    private Set<String> mountPaths;
    @ApiModelProperty(value = "实例ID")
    private String instanceId;
    @ApiModelProperty(value = "实例名")
    private String instanceName;
    @ApiModelProperty(value = "实例挂载路径")
    private Map<String, Set<String>> instanceMountPaths;
    @ApiModelProperty(value = "类型名称")
    private String contentTypeName;

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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public Map<String, Set<String>> getInstanceMountPaths() {
        return instanceMountPaths;
    }

    public void setInstanceMountPaths(Map<String, Set<String>> instanceMountPaths) {
        this.instanceMountPaths = instanceMountPaths;
    }

    public String getContentTypeName() {
        return contentTypeName;
    }

    public void setContentTypeName(String contentTypeName) {
        this.contentTypeName = contentTypeName;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }
}
