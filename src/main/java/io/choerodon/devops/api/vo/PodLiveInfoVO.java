package io.choerodon.devops.api.vo;

import java.util.Date;
import java.util.List;

/**
 * Pod实时信息
 *
 * @author lihao
 * @date 2019-08-15 14:22
 */
public class PodLiveInfoVO {

    private String podName;
    private String podIp;
    private String nodeName;
    private String nodeIp;
    private Long podId;
    private List<ContainerVO> containers;
    private List<String> cpuUsedList;
    private List<String> memoryUsedList;
    private List<Date> timeList;
    private Date creationDate;

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

    public Long getPodId() {
        return podId;
    }

    public void setPodId(Long podId) {
        this.podId = podId;
    }

    public List<ContainerVO> getContainers() {
        return containers;
    }

    public void setContainers(List<ContainerVO> containers) {
        this.containers = containers;
    }

    public List<String> getCpuUsedList() {
        return cpuUsedList;
    }

    public void setCpuUsedList(List<String> cpuUsedList) {
        this.cpuUsedList = cpuUsedList;
    }

    public List<String> getMemoryUsedList() {
        return memoryUsedList;
    }

    public void setMemoryUsedList(List<String> memoryUsedList) {
        this.memoryUsedList = memoryUsedList;
    }

    public List<Date> getTimeList() {
        return timeList;
    }

    public void setTimeList(List<Date> timeList) {
        this.timeList = timeList;
    }
}