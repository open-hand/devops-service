package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * devops环境部署应用表
 */
@Table(name = "devops_deploy_app_center_env")
@ModifyAudit
@VersionAudit
public class DevopsDeployAppCenterEnvDTO extends AuditDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ApiModelProperty(name = "name")
    private String name;

    @ApiModelProperty(name = "code")
    private String code;

    @ApiModelProperty(name = "项目id")
    private Long projectId;

    @ApiModelProperty(name = "部署对象id")
    private Long objectId;

    @ApiModelProperty(name = "Id")
    private Long envId;

    @ApiModelProperty(name = "操作类型")
    private String operationType;

    @ApiModelProperty(name = "chart来源")
    private String chartSource;

    @ApiModelProperty(name = "chart/deployment")
    private String rdupmType;

    private Boolean metricDeployStatus;

    public Boolean getMetricDeployStatus() {
        return metricDeployStatus;
    }

    public void setMetricDeployStatus(Boolean metricDeployStatus) {
        this.metricDeployStatus = metricDeployStatus;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getRdupmType() {
        return rdupmType;
    }

    public void setRdupmType(String rdupmType) {
        this.rdupmType = rdupmType;
    }

    public String getChartSource() {
        return chartSource;
    }

    public void setChartSource(String chartSource) {
        this.chartSource = chartSource;
    }
}