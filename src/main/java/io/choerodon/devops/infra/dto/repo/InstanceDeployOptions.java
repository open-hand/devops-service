package io.choerodon.devops.infra.dto.repo;

import io.swagger.annotations.ApiModelProperty;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/7/1 10:24
 */
public class InstanceDeployOptions {
    @ApiModelProperty("实例名称")
    private String instanceName;
    @ApiModelProperty("当前部署实例id")
    private String instanceId;
    @ApiModelProperty("下载命令")
    private String downloadCommand;
    @ApiModelProperty("前置命令")
    private String preCommand;
    @ApiModelProperty("启动命令")
    private String runCommand;
    @ApiModelProperty("后置命令")
    private String postCommand;
    @ApiModelProperty("删除命令")
    private String killCommand;
    @ApiModelProperty("健康探针")
    private String healthProb;
    @ApiModelProperty("操作类型 create/update")
    private String operation;
    @ApiModelProperty("应用code")
    private String appCode;
    @ApiModelProperty("应用版本")
    private String version;
    @ApiModelProperty("工作目录")
    private String workDir;

    public InstanceDeployOptions() {
    }

    public InstanceDeployOptions(String instanceName, String instanceId, String downloadCommand, String preCommand, String runCommand, String postCommand, String killCommand, String healthProb, String operation, String appCode, String version, String workDir) {
        this.instanceName = instanceName;
        this.instanceId = instanceId;
        this.downloadCommand = downloadCommand;
        this.preCommand = preCommand;
        this.runCommand = runCommand;
        this.postCommand = postCommand;
        this.killCommand = killCommand;
        this.healthProb = healthProb;
        this.operation = operation;
        this.appCode = appCode;
        this.version = version;
        this.workDir = workDir;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDownloadCommand() {
        return downloadCommand;
    }

    public void setDownloadCommand(String downloadCommand) {
        this.downloadCommand = downloadCommand;
    }

    public String getPreCommand() {
        return preCommand;
    }

    public void setPreCommand(String preCommand) {
        this.preCommand = preCommand;
    }

    public String getRunCommand() {
        return runCommand;
    }

    public void setRunCommand(String runCommand) {
        this.runCommand = runCommand;
    }

    public String getPostCommand() {
        return postCommand;
    }

    public void setPostCommand(String postCommand) {
        this.postCommand = postCommand;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getKillCommand() {
        return killCommand;
    }

    public void setKillCommand(String killCommand) {
        this.killCommand = killCommand;
    }

    public String getHealthProb() {
        return healthProb;
    }

    public void setHealthProb(String healthProb) {
        this.healthProb = healthProb;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getWorkDir() {
        return workDir;
    }

    public InstanceDeployOptions setWorkDir(String workDir) {
        this.workDir = workDir;
        return this;
    }
}
