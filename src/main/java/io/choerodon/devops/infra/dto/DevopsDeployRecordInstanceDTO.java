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
 * 这个表是用于存储批量部署的部署纪录与实例的关联关系的
 *
 * @author zmf
 * @since 2/26/20
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_deploy_record_instance")
public class DevopsDeployRecordInstanceDTO extends AuditDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @ApiModelProperty("自增id")
    private Long id;

    @ApiModelProperty("部署纪录id")
    private Long deployRecordId;

    @ApiModelProperty("实例id")
    private Long instanceId;

    @ApiModelProperty("实例code")
    private String instanceCode;

    @ApiModelProperty("实例部署时的版本")
    private String instanceVersion;

    @ApiModelProperty("实例的应用服务id")
    private Long appServiceId;

    @ApiModelProperty("环境id")
    private Long envId;

    public DevopsDeployRecordInstanceDTO() {
    }

    public DevopsDeployRecordInstanceDTO(Long deployRecordId, Long instanceId, String instanceCode, String instanceVersion, Long appServiceId, Long envId) {
        this.deployRecordId = deployRecordId;
        this.instanceId = instanceId;
        this.instanceCode = instanceCode;
        this.instanceVersion = instanceVersion;
        this.appServiceId = appServiceId;
        this.envId = envId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDeployRecordId() {
        return deployRecordId;
    }

    public void setDeployRecordId(Long deployRecordId) {
        this.deployRecordId = deployRecordId;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public String getInstanceCode() {
        return instanceCode;
    }

    public void setInstanceCode(String instanceCode) {
        this.instanceCode = instanceCode;
    }

    public String getInstanceVersion() {
        return instanceVersion;
    }

    public void setInstanceVersion(String instanceVersion) {
        this.instanceVersion = instanceVersion;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }
}
