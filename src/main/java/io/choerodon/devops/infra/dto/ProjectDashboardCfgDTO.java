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
 * 项目质量评分配置表(ProjectDashboardCfg)实体类
 *
 * @author hao.wang@zknow.com
 * @since 2023-06-12 14:38:58
 */

@ApiModel("项目质量评分配置表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_project_dashboard_cfg")
public class ProjectDashboardCfgDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "租户Id", required = true)
    @NotNull
    private Long tenantId;

    @ApiModelProperty(value = "合格分数", required = true)
    @NotNull
    private Double passScore;

    @ApiModelProperty(value = "代码权重", required = true)
    @NotNull
    private Long codeWeight;

    @ApiModelProperty(value = "漏洞权重", required = true)
    @NotNull
    private Long vulnWeight;

    @ApiModelProperty(value = "漏洞权重", required = true)
    @NotNull
    private Long k8sWeight;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Double getPassScore() {
        return passScore;
    }

    public void setPassScore(Double passScore) {
        this.passScore = passScore;
    }

    public Long getCodeWeight() {
        return codeWeight;
    }

    public void setCodeWeight(Long codeWeight) {
        this.codeWeight = codeWeight;
    }

    public Long getVulnWeight() {
        return vulnWeight;
    }

    public void setVulnWeight(Long vulnWeight) {
        this.vulnWeight = vulnWeight;
    }

    public Long getK8sWeight() {
        return k8sWeight;
    }

    public void setK8sWeight(Long k8sWeight) {
        this.k8sWeight = k8sWeight;
    }
}

