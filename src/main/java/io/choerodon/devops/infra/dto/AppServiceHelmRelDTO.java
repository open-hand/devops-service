package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * 应用服务和helm配置的关联关系表(AppServiceHelmRel)实体类
 *
 * @author hao.wang@zknow.com
 * @since 2022-07-15 10:55:51
 */

@ApiModel("应用服务和helm配置的关联关系表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_app_service_helm_rel")
public class AppServiceHelmRelDTO extends AuditDomain {
    private static final long serialVersionUID = 531530280708275480L;

    public static final String FIELD_ID = "id";
    public static final String FIELD_APP_SERVICE_ID = "appServiceId";
    public static final String FIELD_HELM_CONFIG_ID = "helmConfigId";

    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "应用服务id，devops_app_service.id", required = true)
    @NotNull
    private Long appServiceId;

    @ApiModelProperty(value = "配置Id,devops_helm_config.id", required = true)
    @NotNull
    private Long helmConfigId;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Long getHelmConfigId() {
        return helmConfigId;
    }

    public void setHelmConfigId(Long helmConfigId) {
        this.helmConfigId = helmConfigId;
    }

}

