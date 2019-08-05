package io.choerodon.devops.infra.dto;

import java.util.List;

/**
 * @author zmf
 */
public class DevopsApplicationViewDTO {
    private Long id;
    private String name;
    private List<AppServiceInstanceViewDTO> instances;

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

    public List<AppServiceInstanceViewDTO> getInstances() {
        return instances;
    }

    public void setInstances(List<AppServiceInstanceViewDTO> instances) {
        this.instances = instances;
    }
}
