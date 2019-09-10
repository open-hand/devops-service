package io.choerodon.devops.api.vo;

import java.util.Date;

/**
 * Created by Sheep on 2019/9/10.
 */
public class PodMetricsRedisInfoVO {

    private String namespace;
    private String name;
    private String cpu;
    private String memory;
    private Date snapShotTime;
    private String clusterCode;


    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public Date getSnapShotTime() {
        return snapShotTime;
    }

    public void setSnapShotTime(Date snapShotTime) {
        this.snapShotTime = snapShotTime;
    }

    public String getClusterCode() {
        return clusterCode;
    }

    public void setClusterCode(String clusterCode) {
        this.clusterCode = clusterCode;
    }
}
