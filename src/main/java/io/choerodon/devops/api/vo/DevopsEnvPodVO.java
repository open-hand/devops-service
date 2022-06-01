
package io.choerodon.devops.api.vo;

import java.util.Date;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.hzero.starter.keyencrypt.core.Encrypt;

public class DevopsEnvPodVO {
    @Encrypt
    private Long id;
    @Encrypt
    @ApiModelProperty("所属实例id")
    private Long instanceId;
    @ApiModelProperty("pod名")
    private String name;
    @ApiModelProperty("pod ip")
    private String ip;
    @ApiModelProperty("pod 是否就绪")
    private Boolean isReady;
    @ApiModelProperty("pod 状态")
    private String status;
    @ApiModelProperty("pod 创建日期")
    private Date creationDate;
    @ApiModelProperty("所属应用服务名称")
    private String appServiceName;
    @ApiModelProperty("所属命名空间")
    private String namespace;
    @ApiModelProperty("应用服务版本")
    private String appServiceVersion;
    @ApiModelProperty("所属实例编码")
    private String instanceCode;
    @Encrypt
    @ApiModelProperty("环境id")
    private Long envId;
    @ApiModelProperty("项目id")
    private Long projectId;
    @ApiModelProperty("环境编码")
    private String envCode;
    @ApiModelProperty("环境名称")
    private String envName;
    @ApiModelProperty(hidden = true)
    private Long objectVersionNumber;
    @ApiModelProperty("环境是否连接")
    private Boolean isConnect;
    @Encrypt
    @ApiModelProperty("集群id")
    private Long clusterId;
    @ApiModelProperty("容器列表")
    private List<ContainerVO> containers;
    @ApiModelProperty("节点名称")
    private String nodeName;
    @ApiModelProperty("重启次数")
    private Long restartCount;
    @JsonIgnore
    @ApiModelProperty(hidden = true)
    private String ownerKind;
    @JsonIgnore
    @ApiModelProperty(hidden = true)
    private String ownerName;

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

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Boolean getReady() {
        return isReady;
    }

    public void setReady(Boolean ready) {
        isReady = ready;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getAppServiceName() {
        return appServiceName;
    }

    public void setAppServiceName(String appServiceName) {
        this.appServiceName = appServiceName;
    }

    public String getAppServiceVersion() {
        return appServiceVersion;
    }

    public void setAppServiceVersion(String appServiceVersion) {
        this.appServiceVersion = appServiceVersion;
    }

    public String getInstanceCode() {
        return instanceCode;
    }

    public void setInstanceCode(String instanceCode) {
        this.instanceCode = instanceCode;
    }

    public String getEnvCode() {
        return envCode;
    }

    public void setEnvCode(String envCode) {
        this.envCode = envCode;
    }

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Boolean getConnect() {
        return isConnect;
    }

    public void setConnect(Boolean connect) {
        isConnect = connect;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    public List<ContainerVO> getContainers() {
        return containers;
    }

    public void setContainers(List<ContainerVO> containers) {
        this.containers = containers;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public Long getRestartCount() {
        return restartCount;
    }

    public void setRestartCount(Long restartCount) {
        this.restartCount = restartCount;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public String getOwnerKind() {
        return ownerKind;
    }

    public void setOwnerKind(String ownerKind) {
        this.ownerKind = ownerKind;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
}
