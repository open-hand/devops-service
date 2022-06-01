package io.choerodon.devops.api.vo;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * Pod实时信息
 *
 * @author lihao
 * @date 2019-08-15 14:22
 */
public class PodLiveInfoVO {

    @ApiModelProperty("pod名称")
    private String podName;
    @ApiModelProperty("pod ip")
    private String podIp;
    @ApiModelProperty("节点名称")
    private String nodeName;
    @ApiModelProperty("节点ip")
    private String nodeIp;
    @JsonProperty("podId")
    @Encrypt
    private Long id;
    @ApiModelProperty("pod 中的容器列表")
    private List<ContainerVO> containers;
    @ApiModelProperty("cpu使用信息")
    private List<Long> cpuUsedList;
    @ApiModelProperty("内存使用信息")
    private List<Long> memoryUsedList;
    private List<Date> timeList;
    @ApiModelProperty("创建日期")
    private Date creationDate;
    @Encrypt
    @ApiModelProperty("所属集群id")
    private Long clusterId;
    @ApiModelProperty("所属命名空间")
    private String namespace;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public void setPodName(String podName) {
        this.podName = podName;
    }

    public String getPodName() {
        return podName;
    }

    public void setPodIp(String podIp) {
        this.podIp = podIp;
    }

    public String getPodIp() {
        return podIp;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeIp(String nodeIp) {
        this.nodeIp = nodeIp;
    }

    public String getNodeIp() {
        return nodeIp;
    }

    public List<ContainerVO> getContainers() {
        return containers;
    }

    public void setContainers(List<ContainerVO> containers) {
        this.containers = containers;
    }

    public List<Long> getCpuUsedList() {
        return cpuUsedList;
    }

    public void setCpuUsedList(List<Long> cpuUsedList) {
        this.cpuUsedList = cpuUsedList;
    }

    public List<Long> getMemoryUsedList() {
        return memoryUsedList;
    }

    public void setMemoryUsedList(List<Long> memoryUsedList) {
        this.memoryUsedList = memoryUsedList;
    }

    public List<Date> getTimeList() {
        return timeList;
    }

    public void setTimeList(List<Date> timeList) {
        this.timeList = timeList;
    }
}