package io.choerodon.devops.api.vo.deploy;

import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Length;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/7/1 9:10
 */
public class CustomDeployVO {
    @Encrypt
    @ApiModelProperty("主机id")
    private Long hostId;
    /**
     * {@link io.choerodon.devops.infra.enums.AppSourceType}
     */
    @ApiModelProperty("部署来源")
    private String sourceType;
    @ApiModelProperty("应用名")
    @Length(max = 128, min = 1, message = "devops.host.app.name.length")
    private String appName;
    @ApiModelProperty("应用编码")
    @Length(max = 64, min = 1, message = "devops.host.app.code.length")
    private String appCode;
    @ApiModelProperty("来源配置")
    private String sourceConfig;

    @ApiModelProperty("前置命令")
    private String preCommand;
    @ApiModelProperty("运行命令")
    private String runCommand;
    @ApiModelProperty("后置命令")
    private String postCommand;
    @ApiModelProperty("删除命令")
    private String killCommand;
    @ApiModelProperty("健康探针")
    private String healthProb;
    @ApiModelProperty("操作类型 create/update")
    private String operation;
    @ApiModelProperty("部署文件信息")
    private FileInfoVO fileInfoVO;

    private Long appId;

    public CustomDeployVO() {
    }

    public CustomDeployVO(String sourceType, String preCommand, String runCommand, String postCommand) {
        this.sourceType = sourceType;
        this.preCommand = preCommand;
        this.runCommand = runCommand;
        this.postCommand = postCommand;
    }

    public Long getHostId() {
        return hostId;
    }

    public CustomDeployVO setHostId(Long hostId) {
        this.hostId = hostId;
        return this;
    }

    public String getSourceType() {
        return sourceType;
    }

    public CustomDeployVO setSourceType(String sourceType) {
        this.sourceType = sourceType;
        return this;
    }

    public String getAppName() {
        return appName;
    }

    public CustomDeployVO setAppName(String appName) {
        this.appName = appName;
        return this;
    }

    public String getAppCode() {
        return appCode;
    }

    public CustomDeployVO setAppCode(String appCode) {
        this.appCode = appCode;
        return this;
    }

    public String getSourceConfig() {
        return sourceConfig;
    }

    public CustomDeployVO setSourceConfig(String sourceConfig) {
        this.sourceConfig = sourceConfig;
        return this;
    }

    public String getPreCommand() {
        return preCommand;
    }

    public CustomDeployVO setPreCommand(String preCommand) {
        this.preCommand = preCommand;
        return this;
    }

    public String getRunCommand() {
        return runCommand;
    }

    public CustomDeployVO setRunCommand(String runCommand) {
        this.runCommand = runCommand;
        return this;
    }

    public String getPostCommand() {
        return postCommand;
    }

    public CustomDeployVO setPostCommand(String postCommand) {
        this.postCommand = postCommand;
        return this;
    }

    public String getKillCommand() {
        return killCommand;
    }

    public CustomDeployVO setKillCommand(String killCommand) {
        this.killCommand = killCommand;
        return this;
    }

    public String getHealthProb() {
        return healthProb;
    }

    public CustomDeployVO setHealthProb(String healthProb) {
        this.healthProb = healthProb;
        return this;
    }

    public String getOperation() {
        return operation;
    }

    public CustomDeployVO setOperation(String operation) {
        this.operation = operation;
        return this;
    }

    public FileInfoVO getFileInfoVO() {
        return fileInfoVO;
    }

    public CustomDeployVO setFileInfoVO(FileInfoVO fileInfoVO) {
        this.fileInfoVO = fileInfoVO;
        return this;
    }

    public Long getAppId() {
        return appId;
    }

    public CustomDeployVO setAppId(Long appId) {
        this.appId = appId;
        return this;
    }
}
