package io.choerodon.devops.infra.dto;

import java.util.Date;
import javax.persistence.*;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * Created by Sheep on 2019/7/29.
 */
@ModifyAudit
@VersionAudit
@Table(name="devops_deploy_record")
public class DevopsDeployRecordDTO extends AuditDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ApiModelProperty("项目id")
    private Long projectId;
    @ApiModelProperty("部署类型: auto / manual / batch")
    private String deployType;
    @ApiModelProperty("部署id：对于type为manual，是实例的CommandId，对于type是auto，是流水线纪录id，对于type是batch，此值为null")
    private Long deployId;

    private String deployMode;

    private Long deployPayloadId;

    private String deployPayloadName;

    private Date deployTime;

    private String deployResult;

    private String deployObjectType;

    private String deployObjectName;

    private String deployObjectVersion;

    private String instanceName;

    public DevopsDeployRecordDTO() {
    }

    public DevopsDeployRecordDTO(Long projectId, String deployType, Long deployId, String deployMode, Long deployPayloadId, String deployPayloadName, String deployResult, Date deployTime, String deployObjectType, String deployObjectName, String deployObjectVersion, String instanceName) {
        this.projectId = projectId;
        this.deployType = deployType;
        this.deployId = deployId;
        this.deployMode = deployMode;
        this.deployPayloadId = deployPayloadId;
        this.deployPayloadName = deployPayloadName;
        this.deployResult = deployResult;
        this.deployTime = deployTime;
        this.deployObjectType = deployObjectType;
        this.deployObjectName = deployObjectName;
        this.deployObjectVersion = deployObjectVersion;
        this.instanceName = instanceName;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeployType() {
        return deployType;
    }

    public void setDeployType(String deployType) {
        this.deployType = deployType;
    }

    public Long getDeployId() {
        return deployId;
    }

    public void setDeployId(Long deployId) {
        this.deployId = deployId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getDeployResult() {
        return deployResult;
    }

    public void setDeployResult(String deployResult) {
        this.deployResult = deployResult;
    }

    public String getDeployMode() {
        return deployMode;
    }

    public void setDeployMode(String deployMode) {
        this.deployMode = deployMode;
    }

    public Long getDeployPayloadId() {
        return deployPayloadId;
    }

    public void setDeployPayloadId(Long deployPayloadId) {
        this.deployPayloadId = deployPayloadId;
    }

    public String getDeployPayloadName() {
        return deployPayloadName;
    }

    public void setDeployPayloadName(String deployPayloadName) {
        this.deployPayloadName = deployPayloadName;
    }

    public Date getDeployTime() {
        return deployTime;
    }

    public void setDeployTime(Date deployTime) {
        this.deployTime = deployTime;
    }

    public String getDeployObjectType() {
        return deployObjectType;
    }

    public void setDeployObjectType(String deployObjectType) {
        this.deployObjectType = deployObjectType;
    }

    public String getDeployObjectName() {
        return deployObjectName;
    }

    public void setDeployObjectName(String deployObjectName) {
        this.deployObjectName = deployObjectName;
    }

    public String getDeployObjectVersion() {
        return deployObjectVersion;
    }

    public void setDeployObjectVersion(String deployObjectVersion) {
        this.deployObjectVersion = deployObjectVersion;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    @Override
    public String toString() {
        return "DevopsDeployRecordDTO{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", deployType='" + deployType + '\'' +
                ", deployId=" + deployId +
                ", deployMode='" + deployMode + '\'' +
                ", deployPayloadId=" + deployPayloadId +
                ", deployPayloadName='" + deployPayloadName + '\'' +
                ", deployTime=" + deployTime +
                ", deployResult='" + deployResult + '\'' +
                ", deployObjectType='" + deployObjectType + '\'' +
                ", deployObjectName='" + deployObjectName + '\'' +
                ", deployObjectVersion='" + deployObjectVersion + '\'' +
                ", instanceName='" + instanceName + '\'' +
                '}';
    }
}
