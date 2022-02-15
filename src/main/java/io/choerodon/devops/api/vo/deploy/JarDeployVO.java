package io.choerodon.devops.api.vo.deploy;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.vo.market.MarketDeployObjectInfoVO;
import io.choerodon.devops.api.vo.rdupm.ProdJarInfoVO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/7/1 9:10
 */
public class JarDeployVO {
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
    private String operate;

    @ApiModelProperty("部署对象信息")
    private MarketDeployObjectInfoVO marketDeployObjectInfoVO;
    @Encrypt
    private Long mktAppVersionId;
    @Encrypt
    private Long deployObjectId;
    private FileInfoVO fileInfoVO;
    private ProdJarInfoVO prodJarInfoVO;

    @ApiModelProperty("部署配置文件列表")
    private List<ConfigSettingVO> configSettingVOS;

    public JarDeployVO() {
    }

    public JarDeployVO(String sourceType, String appName, String appCode, String preCommand, String runCommand, String postCommand, ProdJarInfoVO prodJarInfoVO) {
        this.appCode = appCode;
        this.appName = appName;
        this.sourceType = sourceType;
        this.prodJarInfoVO = prodJarInfoVO;
        this.preCommand = preCommand;
        this.runCommand = runCommand;
        this.postCommand = postCommand;
    }

    public MarketDeployObjectInfoVO getMarketDeployObjectInfoVO() {
        return marketDeployObjectInfoVO;
    }

    public void setMarketDeployObjectInfoVO(MarketDeployObjectInfoVO marketDeployObjectInfoVO) {
        this.marketDeployObjectInfoVO = marketDeployObjectInfoVO;
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

    public ProdJarInfoVO getProdJarInfoVO() {
        return prodJarInfoVO;
    }

    public void setProdJarInfoVO(ProdJarInfoVO prodJarInfoVO) {
        this.prodJarInfoVO = prodJarInfoVO;
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

    public Long getMktAppVersionId() {
        return mktAppVersionId;
    }

    public void setMktAppVersionId(Long mktAppVersionId) {
        this.mktAppVersionId = mktAppVersionId;
    }

    public Long getDeployObjectId() {
        return deployObjectId;
    }

    public void setDeployObjectId(Long deployObjectId) {
        this.deployObjectId = deployObjectId;
    }

    public String getHealthProb() {
        return healthProb;
    }

    public void setHealthProb(String healthProb) {
        this.healthProb = healthProb;
    }

    public String getKillCommand() {
        return killCommand;
    }

    public void setKillCommand(String killCommand) {
        this.killCommand = killCommand;
    }

    public String getOperate() {
        return operate;
    }

    public void setOperate(String operate) {
        this.operate = operate;
    }
}
