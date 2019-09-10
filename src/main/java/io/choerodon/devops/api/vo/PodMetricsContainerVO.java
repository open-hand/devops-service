package io.choerodon.devops.api.vo;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Sheep on 2019/9/9.
 */
public class PodMetricsContainerVO {

    private String name;
    @SerializedName("Usage")
    private PodMetricsContainerUsageVO usage;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PodMetricsContainerUsageVO getUsage() {
        return usage;
    }

    public void setUsage(PodMetricsContainerUsageVO usage) {
        this.usage = usage;
    }
}
