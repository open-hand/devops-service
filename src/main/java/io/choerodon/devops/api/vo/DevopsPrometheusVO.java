package io.choerodon.devops.api.vo;

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

    @ApiModelProperty("pvNames/必填")
    @NotNull(message = "error.pv.name.null")
    private Map<String,String> pvNames;

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

    public Map<String, String> getPvNames() {
        return pvNames;
    }

    public void setPvNames(Map<String, String> pvNames) {
        this.pvNames = pvNames;
    }

    public String getClusterCode() {
        return clusterCode;
    }

    public void setClusterCode(String clusterCode) {
        this.clusterCode = clusterCode;
    }
}
