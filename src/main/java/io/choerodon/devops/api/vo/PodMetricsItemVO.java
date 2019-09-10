package io.choerodon.devops.api.vo;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Sheep on 2019/9/9.
 */
public class PodMetricsItemVO {
    private String name;
    private String namespace;
    @SerializedName("Containers")
    private List<PodMetricsContainerVO> containers;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public List<PodMetricsContainerVO> getContainers() {
        return containers;
    }

    public void setContainers(List<PodMetricsContainerVO> containers) {
        this.containers = containers;
    }
}
