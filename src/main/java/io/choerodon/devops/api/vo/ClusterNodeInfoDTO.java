package io.choerodon.devops.api.vo;

/**
 * @author zmf
 */
public class ClusterNodeInfoDTO {
    /**
     * values: master/node
     */
    private String type;
    private String nodeName;
    private String status;
    private String createTime;

    private String cpuTotal;
    private String cpuRequest;
    private String cpuLimit;
    private String cpuRequestPercentage;
    private String cpuLimitPercentage;

    private String memoryTotal;
    private String memoryRequest;
    private String memoryLimit;
    private String memoryRequestPercentage;
    private String memoryLimitPercentage;

    private Long podTotal;
    private Long podCount;
    private String podPercentage;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getCpuTotal() {
        return cpuTotal;
    }

    public void setCpuTotal(String cpuTotal) {
        this.cpuTotal = cpuTotal;
    }

    public String getCpuRequest() {
        return cpuRequest;
    }

    public void setCpuRequest(String cpuRequest) {
        this.cpuRequest = cpuRequest;
    }

    public String getCpuLimit() {
        return cpuLimit;
    }

    public void setCpuLimit(String cpuLimit) {
        this.cpuLimit = cpuLimit;
    }

    public String getCpuRequestPercentage() {
        return cpuRequestPercentage;
    }

    public void setCpuRequestPercentage(String cpuRequestPercentage) {
        this.cpuRequestPercentage = cpuRequestPercentage;
    }

    public String getCpuLimitPercentage() {
        return cpuLimitPercentage;
    }

    public void setCpuLimitPercentage(String cpuLimitPercentage) {
        this.cpuLimitPercentage = cpuLimitPercentage;
    }

    public String getMemoryTotal() {
        return memoryTotal;
    }

    public void setMemoryTotal(String memoryTotal) {
        this.memoryTotal = memoryTotal;
    }

    public String getMemoryRequest() {
        return memoryRequest;
    }

    public void setMemoryRequest(String memoryRequest) {
        this.memoryRequest = memoryRequest;
    }

    public String getMemoryLimit() {
        return memoryLimit;
    }

    public void setMemoryLimit(String memoryLimit) {
        this.memoryLimit = memoryLimit;
    }

    public String getMemoryRequestPercentage() {
        return memoryRequestPercentage;
    }

    public void setMemoryRequestPercentage(String memoryRequestPercentage) {
        this.memoryRequestPercentage = memoryRequestPercentage;
    }

    public String getMemoryLimitPercentage() {
        return memoryLimitPercentage;
    }

    public void setMemoryLimitPercentage(String memoryLimitPercentage) {
        this.memoryLimitPercentage = memoryLimitPercentage;
    }

    public Long getPodTotal() {
        return podTotal;
    }

    public void setPodTotal(Long podTotal) {
        this.podTotal = podTotal;
    }

    public Long getPodCount() {
        return podCount;
    }

    public void setPodCount(Long podCount) {
        this.podCount = podCount;
    }

    public String getPodPercentage() {
        return podPercentage;
    }

    public void setPodPercentage(String podPercentage) {
        this.podPercentage = podPercentage;
    }
}
