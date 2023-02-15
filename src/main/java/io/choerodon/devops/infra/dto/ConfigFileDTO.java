package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * 配置文件表(ConfigFile)实体类
 *
 * @author hao.wang@zknow.com
 * @since 2023-02-15 09:25:06
 */

@ApiModel("配置文件表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_config_file")
public class ConfigFileDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_SOURCE_TYPE = "sourceType";
    public static final String FIELD_SOURCE_ID = "sourceId";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_DETAIL_ID = "detailId";
    private static final long serialVersionUID = 571326934490492807L;
    @Id
    @GeneratedValue
    @Encrypt
    private Long id;

    @ApiModelProperty(value = "层级", required = true)
    @NotBlank
    private String sourceType;

    @ApiModelProperty(value = "层级Id", required = true)
    @NotNull
    private Long sourceId;

    @ApiModelProperty(value = "配置名称", required = true)
    @NotBlank
    private String name;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "devops_config_file_detail.id", required = true)
    @NotNull
    private Long detailId;


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

