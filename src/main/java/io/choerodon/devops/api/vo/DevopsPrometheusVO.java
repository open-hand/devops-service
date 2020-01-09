package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModel;
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

    @ApiModelProperty("PrometheusPvId/必填")
    @NotNull(message = "error.prometheus.pv.id.null")
    private Long prometheusPvId;

    @ApiModelProperty("PrometheusPv状态")
    private String prometheusPvStatus;

    @ApiModelProperty("PrometheusPvc名称")
    private String prometheusPvcName;

    @ApiModelProperty("GrafanaPvId/必填")
    @NotNull(message = "error.grafana.pv.id.null")
    private Long grafanaPvId;

    @ApiModelProperty("GrafanaPv状态")
    private String grafanaPvStatus;

    @ApiModelProperty("GrafanaPvc名称")
    private String grafanaPvcName;

    @ApiModelProperty("AlertmanagerPvId/必填")
    @NotNull(message = "error.alertmanager.pv.id.null")
    private Long alertmanagerPvId;

    @ApiModelProperty("AlertmanagerPv状态")
    private String alertmanagerPvStatus;

    @ApiModelProperty("AlertmanagerPvc名称")
    private String alertmanagerPvcName;

    @ApiModelProperty("集群id")
    private Long clusterId;

    @ApiModelProperty("cluster编码")
    private String clusterCode;

    private Long objectVersionNumber;

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

    public Long getPrometheusPvId() {
        return prometheusPvId;
    }

    public void setPrometheusPvId(Long prometheusPvId) {
        this.prometheusPvId = prometheusPvId;
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

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public String getPrometheusPvStatus() {
        return prometheusPvStatus;
    }

    public void setPrometheusPvStatus(String prometheusPvStatus) {
        this.prometheusPvStatus = prometheusPvStatus;
    }

    public String getGrafanaPvStatus() {
        return grafanaPvStatus;
    }

    public void setGrafanaPvStatus(String grafanaPvStatus) {
        this.grafanaPvStatus = grafanaPvStatus;
    }

    public String getAlertmanagerPvStatus() {
        return alertmanagerPvStatus;
    }

    public void setAlertmanagerPvStatus(String alertmanagerPvStatus) {
        this.alertmanagerPvStatus = alertmanagerPvStatus;
    }

    public String getPrometheusPvcName() {
        return prometheusPvcName;
    }

    public void setPrometheusPvcName(String prometheusPvcName) {
        this.prometheusPvcName = prometheusPvcName;
    }

    public String getGrafanaPvcName() {
        return grafanaPvcName;
    }

    public void setGrafanaPvcName(String grafanaPvcName) {
        this.grafanaPvcName = grafanaPvcName;
    }

    public String getAlertmanagerPvcName() {
        return alertmanagerPvcName;
    }

    public void setAlertmanagerPvcName(String alertmanagerPvcName) {
        this.alertmanagerPvcName = alertmanagerPvcName;
    }
}
