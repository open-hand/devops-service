package io.choerodon.devops.infra.dto;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;

/**
 * @author: 25499
 * @date: 2019/10/28 13:56
 * @description:
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_prometheus")
public class DevopsPrometheusDTO extends AuditDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ApiModelProperty("grafana.adminPassword")
    private String adminPassword;

    @ApiModelProperty("grafana.ingress.hosts")
    private String grafanaDomain;

    @ApiModelProperty("PrometheusPvId")
    private Long prometheusPvId;

    @ApiModelProperty("GrafanaPvId")
    private Long grafanaPvId;

    @ApiModelProperty("AlertmanagerPvId")
    private Long alertmanagerPvId;

    @ApiModelProperty("集群id")
    private Long clusterId;

    @ApiModelProperty("是否启用tls")
    private Boolean enableTls;

    @ApiModelProperty("replacement")
    @Transient
    private String clusterCode;

    @Transient
    private DevopsPvDTO prometheusPv;
    @Transient
    private DevopsPvDTO altermanagerPv;
    @Transient
    private DevopsPvDTO grafanaPv;
    @Transient
    private String clientName;

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

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
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

    public DevopsPvDTO getPrometheusPv() {
        return prometheusPv;
    }

    public void setPrometheusPv(DevopsPvDTO prometheusPv) {
        this.prometheusPv = prometheusPv;
    }

    public DevopsPvDTO getAltermanagerPv() {
        return altermanagerPv;
    }

    public void setAltermanagerPv(DevopsPvDTO altermanagerPv) {
        this.altermanagerPv = altermanagerPv;
    }

    public DevopsPvDTO getGrafanaPv() {
        return grafanaPv;
    }

    public void setGrafanaPv(DevopsPvDTO grafanaPv) {
        this.grafanaPv = grafanaPv;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Boolean getEnableTls() {
        return enableTls;
    }

    public void setEnableTls(Boolean enableTls) {
        this.enableTls = enableTls;
    }
}
