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
 * docker compose部署时保存的yaml文件内容(DockerComposeValue)实体类
 *
 * @author hao.wang08@hand-china.com
 * @since 2022-04-07 10:25:55
 */

@ApiModel("docker compose部署时保存的yaml文件内容")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_docker_compose_value")
public class DockerComposeValueDTO extends AuditDomain {
    private static final long serialVersionUID = 641320710110696876L;

    public static final String FIELD_ID = "id";
    public static final String FIELD_APP_ID = "appId";
    public static final String FIELD_REMARK = "remark";
    public static final String FIELD_VALUE = "value";

    @Id
    @GeneratedValue
    @ApiModelProperty(value = "部署配置id", hidden = true)
    @Encrypt
    private Long id;

    @ApiModelProperty(value = "应用id", hidden = true)
    @Encrypt
    private Long appId;

    @ApiModelProperty(value = "部署备注")
    private String remark;

    @ApiModelProperty(value = "部署使用的docker-compose.yaml文件", required = true)
    @NotBlank
    private String value;

    public DockerComposeValueDTO() {
    }

    public DockerComposeValueDTO(@NotNull Long appId, @NotBlank String remark, @NotBlank String value) {
        this.appId = appId;
        this.remark = remark;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}

