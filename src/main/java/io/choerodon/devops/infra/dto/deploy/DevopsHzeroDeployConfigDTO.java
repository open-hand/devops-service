package io.choerodon.devops.infra.dto.deploy;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/7/28 1:43
 */
@Table(name = "devops_hzero_deploy_config")
@ModifyAudit
@VersionAudit
public class DevopsHzeroDeployConfigDTO extends AuditDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ApiModelProperty("部署配置")
    private String values;
    @ApiModelProperty("网络配置")
    private String service;
    @ApiModelProperty("域名配置")
    private String ingress;

    public DevopsHzeroDeployConfigDTO() {
    }

    public DevopsHzeroDeployConfigDTO(String values, String service, String ingress) {
        this.values = values;
        this.service = service;
        this.ingress = ingress;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getIngress() {
        return ingress;
    }

    public void setIngress(String ingress) {
        this.ingress = ingress;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }
}
