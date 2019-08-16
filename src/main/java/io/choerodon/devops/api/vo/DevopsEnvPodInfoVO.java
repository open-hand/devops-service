package io.choerodon.devops.api.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

public class DevopsEnvPodInfoVO {

    @ApiModelProperty("pod名称")
    private String name;
    @ApiModelProperty("实例名称")
    private String instanceName;
    @ApiModelProperty("内存消耗")
    private String memoryUsed;
    @ApiModelProperty("cpu消耗")
    private String cpuUsed;
    @ApiModelProperty("ip地址")
    private String podIp;
    private Date creationDate;
    @JsonIgnore
    private Double cpuValue;
    @JsonIgnore
    private Long memoryValue;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getMemoryUsed() {
        return memoryUsed;
    }

    public void setMemoryUsed(String memoryUsed) {
        this.memoryUsed = memoryUsed;
    }

    public String getCpuUsed() {
        return cpuUsed;
    }

    public void setCpuUsed(String cpuUsed) {
        this.cpuUsed = cpuUsed;
    }

    public String getPodIp() {
        return podIp;
    }

    public void setPodIp(String podIp) {
        this.podIp = podIp;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Double getCpuValue() {
        return cpuValue;
    }

    public void setCpuValue(Double cpuValue) {
        this.cpuValue = cpuValue;
    }

    public Long getMemoryValue() {
        return memoryValue;
    }

    public void setMemoryValue(Long memoryValue) {
        this.memoryValue = memoryValue;
    }
}
