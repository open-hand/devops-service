package io.choerodon.devops.api.vo;

import java.util.Date;
import java.util.List;

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
    private Long id;
    @ApiModelProperty("应用名称")
    private String name;
    @ApiModelProperty("应用编码")
    private String code;

    @Encrypt
    @ApiModelProperty("应用中心 应用id")
    private Long appId;
    @ApiModelProperty("应用名称/同name,前端需要")
    private String appName;
    @ApiModelProperty("应用编码/同code,前端需要")
    private String appCode;

    @Encrypt
    @ApiModelProperty("实例Id")
    private Long instanceId;
    @Encrypt
    @ApiModelProperty("应用服务Id")
    private Long appServiceId;
    @Encrypt
    @ApiModelProperty("应用服务版本Id")
    private Long appServiceVersionId;
    @Encrypt
    @ApiModelProperty("环境id")
    private Long environmentId;
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
    private Integer podCount;
    @ApiModelProperty("实例的运行中的pod的数量")
    private Integer podRunningCount;
    @Encrypt
    @ApiModelProperty("当前实例生效的commandId/可能为null")
    private Long effectCommandId;
    @ApiModelProperty("当前实例生效的版本/可能为null")
    private String effectCommandVersion;
    @ApiModelProperty(value = "集群id", hidden = true)
    @Encrypt
    private Long clusterId;
    @Encrypt
    @ApiModelProperty("实例最新的command的版本id")
    private Long commandVersionId;
    @ApiModelProperty("应用市场所属的实例部署的当前版本是否可用, 如果被删除就是false/只有市场实例需要")
    private Boolean currentVersionAvailable;
    @ApiModelProperty("应用市场所属的实例是否有更新版本可以升级，如果有是true/只有市场实例需要")
    private Boolean upgradeAvailable;
    @ApiModelProperty("应用版本号")
    private String versionName;
    @ApiModelProperty("部署者信息")
    private IamUserDTO creator;
    @ApiModelProperty("更新者信息")
    private IamUserDTO updater;

    @ApiModelProperty("创建时间")
    private Date creationDate;
    @ApiModelProperty("更新时间")
    private Date lastUpdateDate;

    @ApiModelProperty(name = "chart/deployment")
    private String rdupmType;
    private String commandVersion;
    private Boolean envActive;
    private Boolean envConnected;
    @ApiModelProperty(name = "应用是否存在关联的网络，存在则返回false，否则false")
    private Boolean existService;

    @Encrypt
    @ApiModelProperty(name = "市场应用版本id")
    private Long mktAppVersionId;
    @Encrypt
    @ApiModelProperty(name = "部署对象id")
    private Long mktDeployObjectId;
    @ApiModelProperty(name = "是否启用应用监控")
    private Boolean metricDeployStatus;

    @ApiModelProperty(name = "部署组应用配置")
    private DevopsDeployGroupAppConfigVO appConfig;

    @ApiModelProperty(name = "部署组容器配置")
    private List<DevopsDeployGroupContainerConfigVO> containerConfig;

    @ApiModelProperty("是否开启确认副本生效策略，默认为false")
    private Boolean checkValuesPolicy;

    public Boolean getCheckValuesPolicy() {
        return checkValuesPolicy;
    }

    public void setCheckValuesPolicy(Boolean checkValuesPolicy) {
        this.checkValuesPolicy = checkValuesPolicy;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public Boolean getMetricDeployStatus() {
        return metricDeployStatus;
    }

    public void setMetricDeployStatus(Boolean metricDeployStatus) {
        this.metricDeployStatus = metricDeployStatus;
    }

    public Boolean getCurrentVersionAvailable() {
        return currentVersionAvailable;
    }

    public String getEffectCommandVersion() {
        return effectCommandVersion;
    }

    public Boolean getExistService() {
        return existService;
    }

    public void setExistService(Boolean existService) {
        this.existService = existService;
    }

    public void setEffectCommandVersion(String effectCommandVersion) {
        this.effectCommandVersion = effectCommandVersion;
    }

    public void setCurrentVersionAvailable(Boolean currentVersionAvailable) {
        this.currentVersionAvailable = currentVersionAvailable;
    }

    public Boolean getUpgradeAvailable() {
        return upgradeAvailable;
    }

    public void setUpgradeAvailable(Boolean upgradeAvailable) {
        this.upgradeAvailable = upgradeAvailable;
    }

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public IamUserDTO getCreator() {
        return creator;
    }

    public void setCreator(IamUserDTO creator) {
        this.creator = creator;
    }

    public IamUserDTO getUpdater() {
        return updater;
    }

    public void setUpdater(IamUserDTO updater) {
        this.updater = updater;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getRdupmType() {
        return rdupmType;
    }

    public void setRdupmType(String rdupmType) {
        this.rdupmType = rdupmType;
    }

    public Integer getPodCount() {
        return podCount;
    }

    public void setPodCount(Integer podCount) {
        this.podCount = podCount;
    }

    public Integer getPodRunningCount() {
        return podRunningCount;
    }

    public void setPodRunningCount(Integer podRunningCount) {
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

    public String getCommandVersion() {
        return commandVersion;
    }

    public void setCommandVersion(String commandVersion) {
        this.commandVersion = commandVersion;
    }

    public Boolean getEnvActive() {
        return envActive;
    }

    public void setEnvActive(Boolean envActive) {
        this.envActive = envActive;
    }

    public Boolean getEnvConnected() {
        return envConnected;
    }

    public void setEnvConnected(Boolean envConnected) {
        this.envConnected = envConnected;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
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

    public Long getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(Long environmentId) {
        this.environmentId = environmentId;
    }

    public DevopsDeployGroupAppConfigVO getAppConfig() {
        return appConfig;
    }

    public void setAppConfig(DevopsDeployGroupAppConfigVO appConfig) {
        this.appConfig = appConfig;
    }

    public List<DevopsDeployGroupContainerConfigVO> getContainerConfig() {
        return containerConfig;
    }

    public void setContainerConfig(List<DevopsDeployGroupContainerConfigVO> containerConfig) {
        this.containerConfig = containerConfig;
    }

    public Long getMktAppVersionId() {
        return mktAppVersionId;
    }

    public void setMktAppVersionId(Long mktAppVersionId) {
        this.mktAppVersionId = mktAppVersionId;
    }

    public Long getMktDeployObjectId() {
        return mktDeployObjectId;
    }

    public void setMktDeployObjectId(Long mktDeployObjectId) {
        this.mktDeployObjectId = mktDeployObjectId;
    }
}
