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
    public static final String FIELD_TENANT_ID = "tenantId";
    public static final String FIELD_PASS_SCORE = "passScore";
    public static final String FIELD_CODE_SMELL_WEIGHT = "codeSmellWeight";
    public static final String FIELD_BUG_WEIGHT = "bugWeight";
    public static final String FIELD_VULNERABILITY_WEIGHT = "vulnerabilityWeight";
    private static final long serialVersionUID = -62368571445576823L;
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "租户Id", required = true)
    @NotNull
    private Long tenantId;

    @ApiModelProperty(value = "合格分数", required = true)
    @NotNull
    private Double passScore;

    @ApiModelProperty(value = "代码检查权重", required = true)
    @NotNull
    private Long codeSmellWeight;

    @ApiModelProperty(value = "缺陷权重", required = true)
    @NotNull
    private Long bugWeight;

    @ApiModelProperty(value = "漏洞权重", required = true)
    @NotNull
    private Long vulnerabilityWeight;


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

    public Long getCodeSmellWeight() {
        return codeSmellWeight;
    }

    public void setCodeSmellWeight(Long codeSmellWeight) {
        this.codeSmellWeight = codeSmellWeight;
    }

    public Long getBugWeight() {
        return bugWeight;
    }

    public void setBugWeight(Long bugWeight) {
        this.bugWeight = bugWeight;
    }

    public Long getVulnerabilityWeight() {
        return vulnerabilityWeight;
    }

    public void setVulnerabilityWeight(Long vulnerabilityWeight) {
        this.vulnerabilityWeight = vulnerabilityWeight;
    }

}

