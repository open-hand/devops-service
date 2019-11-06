package io.choerodon.devops.infra.dto;

import java.util.List;
import javax.persistence.*;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.api.vo.PvVO;
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

    @ApiModelProperty("pvId")
    private String pvId;

    @ApiModelProperty("pvcId")
    private String pvcId;

    @ApiModelProperty("集群id")
    private Long clusterId;

    @ApiModelProperty("replacement")
    @Transient
    private String clusterCode;

    @ApiModelProperty("pvc")
    @Transient
    private PvVO pvc;

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

    public String getPvId() {
        return pvId;
    }

    public void setPvId(String pvId) {
        this.pvId = pvId;
    }

    public String getPvcId() {
        return pvcId;
    }

    public void setPvcId(String pvcId) {
        this.pvcId = pvcId;
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

    public PvVO getPvc() {
        return pvc;
    }

    public void setPvc(PvVO pvc) {
        this.pvc = pvc;
    }
}
