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
 * 流水线个人token表(PipelinePersonalToken)实体类
 *
 * @author hao.wang@zknow.com
 * @since 2022-12-05 11:20:48
 */

@ApiModel("流水线个人token表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_pipeline_personal_token")
public class PipelinePersonalTokenDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_USER_ID = "userId";
    public static final String FIELD_TOKEN = "token";
    private static final long serialVersionUID = 271782174360764628L;
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "用户id", required = true)
    @NotNull
    private Long userId;

    @ApiModelProperty(value = "令牌", required = true)
    @NotBlank
    private String token;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}

