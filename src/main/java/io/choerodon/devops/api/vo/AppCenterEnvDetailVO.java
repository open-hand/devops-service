package io.choerodon.devops.api.vo;

import java.util.Date;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.infra.dto.iam.IamUserDTO;

/**
 * @Author: scp
 * @Description:
 * @Date: Created in 2021/8/18
 * @Modified By:
 */
public class AppCenterEnvDetailVO {
    @Encrypt
    @ApiModelProperty("应用中心 应用id")
    private Long appCenterId;
    @Encrypt
    @ApiModelProperty("实例Id")
    private Long instanceId;
    @Encrypt
    @ApiModelProperty("应用服务Id")
    private Long appServiceId;
    @Encrypt
    @ApiModelProperty("应用服务版本Id")
    private Long appServiceVersionId;
    @ApiModelProperty("环境code")
    private String envCode;
    @ApiModelProperty("环境名称")
    private String envName;
    @ApiModelProperty("部署方式")
    private String deployWay;
    @ApiModelProperty("部署对象")
    private String deployObject;
    @ApiModelProperty("chart来源")
    private String chartSource;
    @ApiModelProperty("chart来源，应用服务名称")
    private String appServiceName;
    @ApiModelProperty("chart来源，应用服务code")
    private String appServiceCode;
    @ApiModelProperty("chart状态")
    private String objectStatus;
    @ApiModelProperty("实例的所有pod数量")
    private Long podCount;
    @ApiModelProperty("实例的运行中的pod的数量")
    private Long podRunningCount;
    @Encrypt
    @ApiModelProperty("当前实例生效的commandId/可能为null")
    private Long effectCommandId;
    @ApiModelProperty(value = "集群id", hidden = true)
    @Encrypt
    private Long clusterId;
    @Encrypt
    @ApiModelProperty("实例最新的command的版本id")
    private Long commandVersionId;
    private IamUserDTO iamUserDTO;

    @ApiModelProperty("创建时间")
    private Date creationDate;

    @ApiModelProperty(name = "chart/deployment")
    private String rdupmType;

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Long getAppServiceVersionId() {
        return appServiceVersionId;
    }

    public void setAppServiceVersionId(Long appServiceVersionId) {
        this.appServiceVersionId = appServiceVersionId;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    public Long getCommandVersionId() {
        return commandVersionId;
    }

    public void setCommandVersionId(Long commandVersionId) {
        this.commandVersionId = commandVersionId;
    }

    public Long getAppCenterId() {
        return appCenterId;
    }

    public void setAppCenterId(Long appCenterId) {
        this.appCenterId = appCenterId;
    }

    public String getEnvCode() {
        return envCode;
    }

    public void setEnvCode(String envCode) {
        this.envCode = envCode;
    }

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public String getDeployWay() {
        return deployWay;
    }

    public void setDeployWay(String deployWay) {
        this.deployWay = deployWay;
    }

    public String getAppServiceName() {
        return appServiceName;
    }

    public void setAppServiceName(String appServiceName) {
        this.appServiceName = appServiceName;
    }

    public String getAppServiceCode() {
        return appServiceCode;
    }

    public void setAppServiceCode(String appServiceCode) {
        this.appServiceCode = appServiceCode;
    }

    public String getObjectStatus() {
        return objectStatus;
    }

    public void setObjectStatus(String objectStatus) {
        this.objectStatus = objectStatus;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getChartSource() {
        return chartSource;
    }

    public void setChartSource(String chartSource) {
        this.chartSource = chartSource;
    }

    public String getDeployObject() {
        return deployObject;
    }

    public void setDeployObject(String deployObject) {
        this.deployObject = deployObject;
    }

    public IamUserDTO getIamUserDTO() {
        return iamUserDTO;
    }

    public void setIamUserDTO(IamUserDTO iamUserDTO) {
        this.iamUserDTO = iamUserDTO;
    }

    public String getRdupmType() {
        return rdupmType;
    }

    public void setRdupmType(String rdupmType) {
        this.rdupmType = rdupmType;
    }

    public Long getPodCount() {
        return podCount;
    }

    public void setPodCount(Long podCount) {
        this.podCount = podCount;
    }

    public Long getPodRunningCount() {
        return podRunningCount;
    }

    public void setPodRunningCount(Long podRunningCount) {
        this.podRunningCount = podRunningCount;
    }

    public Long getEffectCommandId() {
        return effectCommandId;
    }

    public void setEffectCommandId(Long effectCommandId) {
        this.effectCommandId = effectCommandId;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }
}
