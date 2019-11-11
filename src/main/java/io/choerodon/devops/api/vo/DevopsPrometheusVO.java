package io.choerodon.devops.api.vo;

import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

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

    @ApiModelProperty("pvIds/必填")
    @NotNull(message = "error.pv.name.null")
    private List<PrometheusPVVO> pvs;


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

    public List<PrometheusPVVO> getPvs() {
        return pvs;
    }

    public void setPvs(List<PrometheusPVVO> pvs) {
        this.pvs = pvs;
    }

    public String getClusterCode() {
        return clusterCode;
    }

    public void setClusterCode(String clusterCode) {
        this.clusterCode = clusterCode;
    }
}
