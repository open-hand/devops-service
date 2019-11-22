package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

/**
 * @author: 25499
 * @date: 2019/10/29 8:44
 * @description:
 */
public class DevopsPrometheusVO {

    @ApiModelProperty("promtheusid/更新必填")
    private Long id;

    @ApiModelProperty("admin密码/必填")
    @NotNull(message = "error.admin.password.null")
    private String adminPassword;

    @ApiModelProperty("grafana域名/必填")
    @NotNull(message = "error.grafana.null")
    private String grafanaDomain;

    @ApiModelProperty("PormetheusPvId/必填")
    @NotNull(message = "error.pormetheus.pv.id.null")
    private Long pormetheusPvId;

    @ApiModelProperty("GrafanaPvId/必填")
    @NotNull(message = "error.grafana.pv.id.null")
    private Long grafanaPvId;

    @ApiModelProperty("AlertmanagerPvId/必填")
    @NotNull(message = "error.alertmanager.pv.id.null")
    private Long alertmanagerPvId;


    @ApiModelProperty("cluster编码/必填")
    @NotNull(message = "error.cluster.code.null")
    private String clusterCode;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getClusterCode() {
        return clusterCode;
    }

    public void setClusterCode(String clusterCode) {
        this.clusterCode = clusterCode;
    }

    public Long getPormetheusPvId() {
        return pormetheusPvId;
    }

    public void setPormetheusPvId(Long pormetheusPvId) {
        this.pormetheusPvId = pormetheusPvId;
    }

    public Long getGrafanaPvId() {
        return grafanaPvId;
    }

    public void setGrafanaPvId(Long grafanaPvId) {
        this.grafanaPvId = grafanaPvId;
    }

    public Long getAlertmanagerPvId() {
        return alertmanagerPvId;
    }

    public void setAlertmanagerPvId(Long alertmanagerPvId) {
        this.alertmanagerPvId = alertmanagerPvId;
    }
}
