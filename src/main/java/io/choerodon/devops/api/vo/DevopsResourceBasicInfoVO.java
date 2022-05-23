package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 资源的基本信息，id和名称
 *
 * @author zmf
 */
public class DevopsResourceBasicInfoVO {
    @Encrypt
    private Long id;
    @ApiModelProperty("资源名称")
    private String name;
    @ApiModelProperty("资源状态")
    private String status;
    @ApiModelProperty("资源关联的实例id")
    private Long instanceId;

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public DevopsResourceBasicInfoVO() {
    }

    public DevopsResourceBasicInfoVO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

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
}
