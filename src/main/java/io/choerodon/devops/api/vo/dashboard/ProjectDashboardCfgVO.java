package io.choerodon.devops.api.vo.dashboard;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author hao.wang@zknow.com
 * @since 2023-06-12 14:43:16
 */
public class ProjectDashboardCfgVO {

    private Long id;
    @ApiModelProperty(value = "租户Id", required = true)
    private Long tenantId;
    @ApiModelProperty(value = "合格分数", required = true)
    private Double passScore;
    @ApiModelProperty(value = "代码检查权重", required = true)
    private Long codeSmellWeight;
    @ApiModelProperty(value = "缺陷权重", required = true)
    private Long bugWeight;
    @ApiModelProperty(value = "漏洞权重", required = true)
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
