package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * 流水线配置的docker认证配置(CiDockerAuthConfig)实体类
 *
 * @author hao.wang08@hand-china.com
 * @since 2022-03-15 09:54:19
 */

@ApiModel("流水线配置的docker认证配置")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_ci_docker_auth_config")
public class CiDockerAuthConfigDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_DEVOPS_PIPELINE_ID = "devopsPipelineId";
    public static final String FIELD_DOMAIN = "domain";
    private static final long serialVersionUID = 989629751337871365L;
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "流水线id", required = true)
    @NotNull
    private Long devopsPipelineId;

    @ApiModelProperty(value = "docker registry 域名", required = true)
    @NotBlank
    private String domain;

    @ApiModelProperty(value = "用户名", required = true)
    @NotBlank
    private String username;

    @ApiModelProperty(value = "密码", required = true)
    @NotBlank
    private String password;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDevopsPipelineId() {
        return devopsPipelineId;
    }

    public void setDevopsPipelineId(Long devopsPipelineId) {
        this.devopsPipelineId = devopsPipelineId;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}

