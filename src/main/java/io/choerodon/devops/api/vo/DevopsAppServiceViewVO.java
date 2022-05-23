package io.choerodon.devops.api.vo;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author zmf
 */
public class DevopsAppServiceViewVO {
    @Encrypt
    private Long id;
    @ApiModelProperty("应用服务名称")
    private String name;
    @ApiModelProperty("应用服务类型")
    private String type;
    @ApiModelProperty("应用服务下的实例")
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setInstances(List<DevopsAppServiceInstanceViewVO> instances) {
        this.instances = instances;
    }
}
