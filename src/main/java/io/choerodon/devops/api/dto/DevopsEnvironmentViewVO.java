package io.choerodon.devops.api.dto;


import java.util.List;

/**
 * @author zmf
 */
public class DevopsEnvironmentViewVO {
    private Long id;
    private String name;
    private Boolean connect;
    private Boolean synchronize;
    private List<DevopsApplicationViewVO> apps;

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

    public List<DevopsApplicationViewVO> getApps() {
        return apps;
    }

    public void setApps(List<DevopsApplicationViewVO> apps) {
        this.apps = apps;
    }

    public Boolean getConnect() {
        return connect;
    }

    public void setConnect(Boolean connect) {
        this.connect = connect;
    }

    public Boolean getSynchronize() {
        return synchronize;
    }

    public void setSynchronize(Boolean synchronize) {
        this.synchronize = synchronize;
    }
}
