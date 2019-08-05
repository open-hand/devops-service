package io.choerodon.devops.api.vo;

import java.util.List;

/**
 * @author zmf
 */
public class DevopsAppServiceViewVO {
    private Long id;
    private String name;
    private List<DevopsAppServiceInstanceViewVO> instances;

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

    public List<DevopsAppServiceInstanceViewVO> getInstances() {
        return instances;
    }

    public void setInstances(List<DevopsAppServiceInstanceViewVO> instances) {
        this.instances = instances;
    }
}
