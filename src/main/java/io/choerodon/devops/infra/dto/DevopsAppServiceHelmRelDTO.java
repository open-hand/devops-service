package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

@Table(name = "devops_app_service_helm_rel")
@ModifyAudit
@VersionAudit
public class DevopsAppServiceHelmRelDTO extends AuditDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Encrypt
    private Long id;
    @ApiModelProperty("应用服务id")
    private Long appServiceId;
    @ApiModelProperty("helm仓库id")
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
