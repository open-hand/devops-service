package io.choerodon.devops.api.vo;

import java.util.Date;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.vo.deploy.DeploySourceVO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/10/12 15:14
 */
public class DeployRecordVO {
    @Encrypt
    @ApiModelProperty("记录id")
    private Long id;
    @ApiModelProperty("界面展示id")
    private String viewId;
    @ApiModelProperty("部署类型: auto / manual / batch")
    private String deployType;
    @ApiModelProperty("部署结果")
    private String deployResult;

    @Encrypt
    @ApiModelProperty("实例id")
    private Long instanceId;

    @Encrypt
    @ApiModelProperty("应用id")
    private Long appId;
    @ApiModelProperty("应用名称")
    private String appName;
    @ApiModelProperty("应用编码")
    private String appCode;
    @ApiModelProperty("部署模式， env 环境部署，host主机部署")
    private String deployMode;

    @Encrypt
    @ApiModelProperty("部署载体id, 主机id or 环境id")
    private Long deployPayloadId;
    @ApiModelProperty("部署载体name 主机名/环境名")
    private String deployPayloadName;
    @ApiModelProperty("部署时间")
    private Date deployTime;
    @ApiModelProperty("部署对象类型 app 应用服务，jar ,image")
    private String deployObjectType;
    @ApiModelProperty("部署载体id, 主机id or 环境id")
    private String deployObjectName;
    @ApiModelProperty("部署对象名")
    private String deployObjectVersion;
    @ApiModelProperty("集群id")
    private Long clusterId;
    @ApiModelProperty("集群是否连接")
    private Boolean connect;

    @Encrypt
    @ApiModelProperty("应用服务id")
    private Long appServiceId;

    @Encrypt
    @ApiModelProperty("环境id")
    private Long envId;

    @ApiModelProperty("部署者")
    private IamUserDTO executeUser;
    @ApiModelProperty(hidden = true)
    private Long createdBy;
    @ApiModelProperty("操作状态")
    private String commandStatus;
    @ApiModelProperty("部署来源")
    private DeploySourceVO deploySourceVO;
    @ApiModelProperty("部署来源")
    private String deploySource;
    @ApiModelProperty("错误消息")
    private String errorMessage;
    @ApiModelProperty("日志")
    private String log;

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    private String errorMsg;

    public String getDeploySource() {
        return deploySource;
    }

    public void setDeploySource(String deploySource) {
        this.deploySource = deploySource;
    }

    public DeploySourceVO getDeploySourceVO() {
        return deploySourceVO;
    }

    public void setDeploySourceVO(DeploySourceVO deploySourceVO) {
        this.deploySourceVO = deploySourceVO;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getViewId() {
        return viewId;
    }

    public void setViewId(String viewId) {
        this.viewId = viewId;
    }

    public String getDeployType() {
        return deployType;
    }

    public void setDeployType(String deployType) {
        this.deployType = deployType;
    }

    public String getDeployResult() {
        return deployResult;
    }

    public void setDeployResult(String deployResult) {
        this.deployResult = deployResult;
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

    public IamUserDTO getExecuteUser() {
        return executeUser;
    }

    public void setExecuteUser(IamUserDTO executeUser) {
        this.executeUser = executeUser;
    }

    public Date getDeployTime() {
        return deployTime;
    }

    public void setDeployTime(Date deployTime) {
        this.deployTime = deployTime;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    public Boolean getConnect() {
        return connect;
    }

    public void setConnect(Boolean connect) {
        this.connect = connect;
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

    public String getCommandStatus() {
        return commandStatus;
    }

    public void setCommandStatus(String commandStatus) {
        this.commandStatus = commandStatus;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
