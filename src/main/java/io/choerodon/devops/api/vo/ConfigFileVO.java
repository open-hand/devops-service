package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * @author hao.wang@zknow.com
 * @since 2023-02-15 09:27:44
 */
public class ConfigFileVO extends AuditDomain {

    @Encrypt
    @ApiModelProperty(hidden = true)
    private Long id;
    @ApiModelProperty(value = "层级", hidden = true)
    private String sourceType;
    @ApiModelProperty(value = "层级Id", hidden = true)
    private Long sourceId;
    @ApiModelProperty(value = "配置名称", required = true)
    private String name;
    @ApiModelProperty(value = "描述")
    private String description;
    @ApiModelProperty(value = "devops_config_file_detail.id", hidden = true)
    private Long detailId;

    private String message;

    private IamUserDTO creator;

    public IamUserDTO getCreator() {
        return creator;
    }

    public void setCreator(IamUserDTO creator) {
        this.creator = creator;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
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

    public Long getDetailId() {
        return detailId;
    }

    public void setDetailId(Long detailId) {
        this.detailId = detailId;
    }
}
