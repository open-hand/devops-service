package io.choerodon.devops.api.vo.deploy;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
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
    private String appName;
    @ApiModelProperty("应用编码")
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

    private FileInfoVO fileInfoVO;

    @ApiModelProperty("部署配置文件列表")
    private List<ConfigSettingVO> configSettingVOS;

    private Long appId;

    public CustomDeployVO() {
    }

    public CustomDeployVO(String sourceType, String preCommand, String runCommand, String postCommand) {
        this.sourceType = sourceType;
        this.preCommand = preCommand;
        this.runCommand = runCommand;
        this.postCommand = postCommand;
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

    public FileInfoVO getFileInfoVO() {
        return fileInfoVO;
    }

    public void setFileInfoVO(FileInfoVO fileInfoVO) {
        this.fileInfoVO = fileInfoVO;
    }

    public String getSourceConfig() {
        return sourceConfig;
    }

    public void setSourceConfig(String sourceConfig) {
        this.sourceConfig = sourceConfig;
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

    public String getSourceType() {
        return sourceType;
    }

    public Long getHostId() {
        return hostId;
    }

    public void setHostId(Long hostId) {
        this.hostId = hostId;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public List<ConfigSettingVO> getConfigSettingVOS() {
        return configSettingVOS;
    }

    public void setConfigSettingVOS(List<ConfigSettingVO> configSettingVOS) {
        this.configSettingVOS = configSettingVOS;
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

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }
}
