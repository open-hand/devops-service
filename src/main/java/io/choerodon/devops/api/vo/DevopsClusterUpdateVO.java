package io.choerodon.devops.api.vo;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 更新集群
 *
 * @author zmf
 */
public class DevopsClusterUpdateVO {
    @Encrypt
    @ApiModelProperty("集群id/必需")
    @NotNull(message = "error.cluster.id.null")
    private Long id;

    @ApiModelProperty("集群名称")
    private String name;

    @ApiModelProperty("集群描述")
    private String description;

    @ApiModelProperty("纪录版本字段/更新时必需")
    @NotNull(message = "error.object.version.number.null")
    private Long objectVersionNumber;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }
}
