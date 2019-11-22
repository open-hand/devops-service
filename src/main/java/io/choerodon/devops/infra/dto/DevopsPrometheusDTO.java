package io.choerodon.devops.infra.dto;

import java.util.List;
import javax.persistence.*;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.entity.BaseDTO;

/**
 * @author: 25499
 * @date: 2019/10/28 13:56
 * @description:
 */
@Table(name = "devops_prometheus")
public class DevopsPrometheusDTO extends BaseDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ApiModelProperty("grafana.adminPassword")
    private String adminPassword;

    @ApiModelProperty("grafana.ingress.hosts")
    private String grafanaDomain;

    @ApiModelProperty("PormetheusPvId")
    private Long pormetheusPvId;

    @ApiModelProperty("GrafanaPvId")
    private Long grafanaPvId;

    @ApiModelProperty("AlertmanagerPvId")
    private Long alertmanagerPvId;

    @ApiModelProperty("集群id")
    private Long clusterId;

    @ApiModelProperty("replacement")
    @Transient
    private String clusterCode;

    @ApiModelProperty("pvc")
    @Transient
    private List<DevopsPvcDTO> devopsPvcDTO;

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

    public List<DevopsPvcDTO> getDevopsPvcDTO() {
        return devopsPvcDTO;
    }

    public void setDevopsPvcDTO(List<DevopsPvcDTO> devopsPvcDTO) {
        this.devopsPvcDTO = devopsPvcDTO;
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
