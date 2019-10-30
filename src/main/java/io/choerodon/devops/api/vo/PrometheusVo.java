package io.choerodon.devops.api.vo;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author: 25499
 * @date: 2019/10/29 8:44
 * @description:
 */
public class PrometheusVo {

    private Long prometheusId;
    @ApiModelProperty("admin密码/必填")
    @NotNull(message = "error.admin.password.null")
    private String adminPassword;
    @ApiModelProperty("grafana域名/必填")
    @NotNull(message = "error.grafana域名.null")
    private String grafanaDomain;
    @ApiModelProperty("pv名称/必填")
    @NotNull(message = "error.pv.name.null")
    private String pvName;
    @ApiModelProperty("cluster名称/必填")
    @NotNull(message = "error.cluster.name.null")
    private String clusterName;


    public Long getPrometheusId() {
        return prometheusId;
    }

    public void setPrometheusId(Long prometheusId) {
        this.prometheusId = prometheusId;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public String getGrafanaDomain() {
        return grafanaDomain;
    }

    public void setGrafanaDomain(String grafanaDomain) {
        this.grafanaDomain = grafanaDomain;
    }

    public String getPvName() {
        return pvName;
    }

    public void setPvName(String pvName) {
        this.pvName = pvName;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }
}
