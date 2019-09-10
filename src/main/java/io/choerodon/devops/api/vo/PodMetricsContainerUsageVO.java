package io.choerodon.devops.api.vo;

/**
 * Created by Sheep on 2019/9/9.
 */
public class PodMetricsContainerUsageVO {

    private String cpu;
    private String memory;

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
}
