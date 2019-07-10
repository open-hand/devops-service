package io.choerodon.devops.api.dto;

import java.util.List;

/**
 * @author zmf
 */
public class DevopsApplicationViewVO {
    private Long id;
    private String name;
    private List<DevopsAppInstanceViewVO> instances;

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

    public List<DevopsAppInstanceViewVO> getInstances() {
        return instances;
    }

    public void setInstances(List<DevopsAppInstanceViewVO> instances) {
        this.instances = instances;
    }
}
