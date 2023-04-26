package io.choerodon.devops.api.vo.deploy;

import io.choerodon.devops.api.vo.market.MarketDeployObjectInfoVO;
import io.choerodon.devops.api.vo.rdupm.ProdJarInfoVO;
import io.choerodon.devops.infra.dto.repo.JarPullInfoDTO;
import io.swagger.annotations.ApiModelProperty;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.hzero.starter.keyencrypt.core.Encrypt;

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
    private String operation;

    @ApiModelProperty("部署对象信息")
    private MarketDeployObjectInfoVO marketDeployObjectInfoVO;
    @Encrypt
    @ApiModelProperty("市场应用版本id")
    private Long mktAppVersionId;
    @Encrypt
    @ApiModelProperty("市场应用部署对象id")
    private Long deployObjectId;

    @ApiModelProperty("上传jar包信息")
    private FileInfoVO fileInfoVO;
    @ApiModelProperty("制品库jar包信息")
    private ProdJarInfoVO prodJarInfoVO;
    @ApiModelProperty("jar包拉取信息")
    private JarPullInfoDTO jarPullInfoDTO;

    @ApiModelProperty("工作目录")
    private String workDir;

    @JsonIgnore
    @ApiModelProperty(hidden = true)
    private Long appId;

    public JarDeployVO() {
    }

    public JarDeployVO(String sourceType, String appName, String appCode, String preCommand, String runCommand, String postCommand, String killCommand, String healthProb, ProdJarInfoVO prodJarInfoVO, String operation) {
        this.appCode = appCode;
        this.appName = appName;
        this.sourceType = sourceType;
        this.prodJarInfoVO = prodJarInfoVO;
        this.preCommand = preCommand;
        this.runCommand = runCommand;
        this.postCommand = postCommand;
        this.healthProb = healthProb;
        this.killCommand = killCommand;
        this.operation = operation;
    }

    public JarDeployVO(String sourceType, String appName, String appCode, String preCommand, String runCommand, String postCommand, String killCommand, String healthProb, JarPullInfoDTO jarPullInfoDTO, String operation) {
        this.appCode = appCode;
        this.appName = appName;
        this.sourceType = sourceType;
        this.jarPullInfoDTO = jarPullInfoDTO;
        this.preCommand = preCommand;
        this.runCommand = runCommand;
        this.postCommand = postCommand;
        this.healthProb = healthProb;
        this.killCommand = killCommand;
        this.operation = operation;
    }

    public Long getHostId() {
        return hostId;
    }

    public JarDeployVO setHostId(Long hostId) {
        this.hostId = hostId;
        return this;
    }

    public String getSourceType() {
        return sourceType;
    }

    public JarDeployVO setSourceType(String sourceType) {
        this.sourceType = sourceType;
        return this;
    }

    public String getAppName() {
        return appName;
    }

    public JarDeployVO setAppName(String appName) {
        this.appName = appName;
        return this;
    }

    public String getAppCode() {
        return appCode;
    }

    public JarDeployVO setAppCode(String appCode) {
        this.appCode = appCode;
        return this;
    }

    public String getSourceConfig() {
        return sourceConfig;
    }

    public JarDeployVO setSourceConfig(String sourceConfig) {
        this.sourceConfig = sourceConfig;
        return this;
    }

    public String getPreCommand() {
        return preCommand;
    }

    public JarDeployVO setPreCommand(String preCommand) {
        this.preCommand = preCommand;
        return this;
    }

    public String getRunCommand() {
        return runCommand;
    }

    public JarDeployVO setRunCommand(String runCommand) {
        this.runCommand = runCommand;
        return this;
    }

    public String getPostCommand() {
        return postCommand;
    }

    public JarDeployVO setPostCommand(String postCommand) {
        this.postCommand = postCommand;
        return this;
    }

    public String getKillCommand() {
        return killCommand;
    }

    public JarDeployVO setKillCommand(String killCommand) {
        this.killCommand = killCommand;
        return this;
    }

    public String getHealthProb() {
        return healthProb;
    }

    public JarDeployVO setHealthProb(String healthProb) {
        this.healthProb = healthProb;
        return this;
    }

    public String getOperation() {
        return operation;
    }

    public JarDeployVO setOperation(String operation) {
        this.operation = operation;
        return this;
    }

    public MarketDeployObjectInfoVO getMarketDeployObjectInfoVO() {
        return marketDeployObjectInfoVO;
    }

    public JarDeployVO setMarketDeployObjectInfoVO(MarketDeployObjectInfoVO marketDeployObjectInfoVO) {
        this.marketDeployObjectInfoVO = marketDeployObjectInfoVO;
        return this;
    }

    public Long getMktAppVersionId() {
        return mktAppVersionId;
    }

    public JarDeployVO setMktAppVersionId(Long mktAppVersionId) {
        this.mktAppVersionId = mktAppVersionId;
        return this;
    }

    public Long getDeployObjectId() {
        return deployObjectId;
    }

    public JarDeployVO setDeployObjectId(Long deployObjectId) {
        this.deployObjectId = deployObjectId;
        return this;
    }

    public FileInfoVO getFileInfoVO() {
        return fileInfoVO;
    }

    public JarDeployVO setFileInfoVO(FileInfoVO fileInfoVO) {
        this.fileInfoVO = fileInfoVO;
        return this;
    }

    public ProdJarInfoVO getProdJarInfoVO() {
        return prodJarInfoVO;
    }

    public JarDeployVO setProdJarInfoVO(ProdJarInfoVO prodJarInfoVO) {
        this.prodJarInfoVO = prodJarInfoVO;
        return this;
    }

    public JarPullInfoDTO getJarPullInfoDTO() {
        return jarPullInfoDTO;
    }

    public JarDeployVO setJarPullInfoDTO(JarPullInfoDTO jarPullInfoDTO) {
        this.jarPullInfoDTO = jarPullInfoDTO;
        return this;
    }

    public Long getAppId() {
        return appId;
    }

    public JarDeployVO setAppId(Long appId) {
        this.appId = appId;
        return this;
    }

    public String getWorkDir() {
        return workDir;
    }

    public JarDeployVO setWorkDir(String workDir) {
        this.workDir = workDir;
        return this;
    }
}
