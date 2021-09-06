package io.choerodon.devops.infra.dto.repo;

import io.swagger.annotations.ApiModelProperty;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/7/1 10:24
 */
public class JavaDeployDTO {
    @ApiModelProperty("实例名称")
    private String instanceName;
    @ApiModelProperty("当前部署实例id")
    private String instanceId;
    @ApiModelProperty("当前进程id，不为空则表示更新，agent会先kill 进程再部署")
    private String pid;

    @ApiModelProperty("下载命令")
    private String downloadCommand;
    @ApiModelProperty("前置命令")
    private String preCommand;
    @ApiModelProperty("启动命令")
    private String runAppCommand;
    @ApiModelProperty("后置命令")
    private String postCommand;


    public JavaDeployDTO(String instanceName, String instanceId, String downloadCommand, String preCommand, String runAppCommand, String postCommand, String pid) {
        this.instanceName = instanceName;
        this.instanceId = instanceId;
        this.downloadCommand = downloadCommand;
        this.preCommand = preCommand;
        this.runAppCommand = runAppCommand;
        this.postCommand = postCommand;
        this.pid = pid;
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

    public String getRunAppCommand() {
        return runAppCommand;
    }

    public void setRunAppCommand(String runAppCommand) {
        this.runAppCommand = runAppCommand;
    }

    public String getPostCommand() {
        return postCommand;
    }

    public void setPostCommand(String postCommand) {
        this.postCommand = postCommand;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
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
}
