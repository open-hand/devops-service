package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * 配置文件详情表(ConfigFileDetail)实体类
 *
 * @author hao.wang@zknow.com
 * @since 2023-02-15 09:25:17
 */

@ApiModel("配置文件详情表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_config_file_detail")
public class ConfigFileDetailDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_MESSAGE = "message";
    private static final long serialVersionUID = 999347056471155163L;
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "配置文件详情")
    private String message;

    public ConfigFileDetailDTO() {
    }

    public ConfigFileDetailDTO(String message) {
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}

