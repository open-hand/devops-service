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
    @ApiModelProperty(value = "代码权重", required = true)
    private Long codeWeight;
    @ApiModelProperty(value = "k8s权重", required = true)
    private Long k8sWeight;
    @ApiModelProperty(value = "漏洞权重", required = true)
    private Long vulnWeight;

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

    public Long getK8sWeight() {
        return k8sWeight;
    }

    public void setK8sWeight(Long k8sWeight) {
        this.k8sWeight = k8sWeight;
    }

    public Long getVulnWeight() {
        return vulnWeight;
    }

    public void setVulnWeight(Long vulnWeight) {
        this.vulnWeight = vulnWeight;
    }
}
