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
 * Created by Zenger on 2018/4/14.
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_service")
public class DevopsServiceDTO extends AuditDomain {
    public static final String ENCRYPT_KEY = "devops_service";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
//    @Encrypt(DevopsServiceDTO.ENCRYPT_KEY)
    private Long id;
    private Long envId;
    private Long appServiceId;
    private Long commandId;
    private String name;
    private String status;
    private String ports;
    private String endPoints;
    private String type;
    private String externalIp;
    private String selectors;
    private String annotations;
    private String loadBalanceIp;

    @ApiModelProperty("所属实例id")
    private Long instanceId;

    @ApiModelProperty("目标对象是应用服务下所有实例时，应用服务的id")
    private Long targetAppServiceId;

    @ApiModelProperty("目标对象是单个实例时，实例code")
    private String targetInstanceCode;


    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExternalIp() {
        return externalIp;
    }

    public void setExternalIp(String externalIp) {
        this.externalIp = externalIp;
    }

    public String getAnnotations() {
        return annotations;
    }

    public void setAnnotations(String annotations) {
        this.annotations = annotations;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPorts() {
        return ports;
    }

    public void setPorts(String ports) {
        this.ports = ports;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }

    public String getEndPoints() {
        return endPoints;
    }

    public void setEndPoints(String endPoints) {
        this.endPoints = endPoints;
    }

    public String getLoadBalanceIp() {
        return loadBalanceIp;
    }

    public void setLoadBalanceIp(String loadBalanceIp) {
        this.loadBalanceIp = loadBalanceIp;
    }

    public Long getTargetAppServiceId() {
        return targetAppServiceId;
    }

    public void setTargetAppServiceId(Long targetAppServiceId) {
        this.targetAppServiceId = targetAppServiceId;
    }

    public String getTargetInstanceCode() {
        return targetInstanceCode;
    }

    public void setTargetInstanceCode(String targetInstanceCode) {
        this.targetInstanceCode = targetInstanceCode;
    }

    public String getSelectors() {
        return selectors;
    }

    public void setSelectors(String selectors) {
        this.selectors = selectors;
    }
}
