package io.choerodon.devops.api.vo;

import java.util.Date;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.nutz.json.JsonIgnore;

/**
 * 展示实例详情页面的单个实例的信息
 *
 * @author zmf
 */
public class AppServiceInstanceInfoVO {
    @Encrypt
    @ApiModelProperty("实例id")
    private Long id;
    @ApiModelProperty("实例code")
    private String code;
    private String status;
    @ApiModelProperty("实例的所有pod数量")
    private Long podCount;
    @ApiModelProperty("实例的运行中的pod的数量")
    private Long podRunningCount;
    @ApiModelProperty("实例所属应用服务id")
    @Encrypt
    private Long appServiceId;
    @ApiModelProperty("实例所属应用服务的名称")
    private String appServiceName;
    /**
     * {@link io.choerodon.devops.infra.enums.AppServiceType}
     */
    @ApiModelProperty("实例所属应用服务的类型/normal_service,share_service")
    private String appServiceType;
    private Long appServiceVersionId;
    private String versionName;

    @ApiModelProperty("最后更新时间")
    private Date lastUpdateDate;
    @ApiModelProperty("实例纪录版本号")
    private Long objectVersionNumber;
    @ApiModelProperty("实例所属环境是否连接")
    private Boolean connect;
    @ApiModelProperty("实例最新的command的版本id")
    private Long commandVersionId;
    @ApiModelProperty("实例最新的command所对应的应用服务版本")
    private String commandVersion;
    @ApiModelProperty("实例最新的command所对应的操作类型")
    private String commandType;
    @ApiModelProperty("实例最新的command的状态")
    private String commandStatus;
    @ApiModelProperty("实例最新的command的错误信息")
    private String error;
    @ApiModelProperty("实例所属项目id")
    private Long projectId;
    @ApiModelProperty("当前实例生效的commandId/可能为null")
    private Long effectCommandId;
    @ApiModelProperty("当前实例生效的版本/可能为null")
    private String effectCommandVersion;
    @ApiModelProperty("当前实例生效的command的状态/可能为null，为")
    private String effectCommandStatus;

    @JsonIgnore
    @ApiModelProperty(value = "集群id", hidden = true)
    private Long clusterId;

    public Long getEffectCommandId() {
        return effectCommandId;
    }

    public void setEffectCommandId(Long effectCommandId) {
        this.effectCommandId = effectCommandId;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getAppServiceName() {
        return appServiceName;
    }

    public void setAppServiceName(String appServiceName) {
        this.appServiceName = appServiceName;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public Boolean getConnect() {
        return connect;
    }

    public void setConnect(Boolean connect) {
        this.connect = connect;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public Long getCommandVersionId() {
        return commandVersionId;
    }

    public void setCommandVersionId(Long commandVersionId) {
        this.commandVersionId = commandVersionId;
    }

    public String getCommandStatus() {
        return commandStatus;
    }

    public void setCommandStatus(String commandStatus) {
        this.commandStatus = commandStatus;
    }

    public String getCommandVersion() {
        return commandVersion;
    }

    public void setCommandVersion(String commandVersion) {
        this.commandVersion = commandVersion;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getAppServiceType() {
        return appServiceType;
    }

    public void setAppServiceType(String appServiceType) {
        this.appServiceType = appServiceType;
    }

    public String getEffectCommandVersion() {
        return effectCommandVersion;
    }

    public void setEffectCommandVersion(String effectCommandVersion) {
        this.effectCommandVersion = effectCommandVersion;
    }

    public String getEffectCommandStatus() {
        return effectCommandStatus;
    }

    public void setEffectCommandStatus(String effectCommandStatus) {
        this.effectCommandStatus = effectCommandStatus;
    }
}
