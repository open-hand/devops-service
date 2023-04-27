package io.choerodon.devops.api.vo.host;

import io.choerodon.devops.api.vo.deploy.FileInfoVO;
import io.choerodon.devops.api.vo.market.MarketDeployObjectInfoVO;
import io.choerodon.devops.api.vo.rdupm.ProdJarInfoVO;
import io.choerodon.devops.infra.dto.DevopsHostCommandDTO;
import io.choerodon.devops.infra.dto.DevopsHostDTO;
import io.choerodon.devops.infra.dto.DockerComposeValueDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.repo.JarPullInfoDTO;
import io.choerodon.devops.infra.enums.deploy.OperationTypeEnum;
import io.choerodon.devops.infra.util.Base64Util;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import java.util.Date;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/8/20 16:08
 */
public class DevopsHostAppVO {
    @ApiModelProperty("项目id")
    private Long projectId;
    @Encrypt
    @ApiModelProperty("主机id")
    private Long hostId;
    @ApiModelProperty("应用名称")
    private String name;
    @ApiModelProperty("应用编码")
    private String code;
    @ApiModelProperty("部署来源")
    private String sourceType;
    @ApiModelProperty("制品类型")
    private String rdupmType;
    /**
     * {@link OperationTypeEnum}
     */
    @ApiModelProperty("操作类型")
    private String operationType;
    @ApiModelProperty("来源配置")
    private String sourceConfig;
    @ApiModelProperty("主机名称")
    private String hostName;
    @ApiModelProperty("应用实例id")
    @Encrypt
    private Long id;
    @ApiModelProperty("部署方式")
    private String deployWay;
    @Encrypt
    @ApiModelProperty("实例id")
    private Long instanceId;

    @ApiModelProperty("前置命令")
    private String preCommand;
    @ApiModelProperty("运行命令")
    private String runCommand;
    @ApiModelProperty("后置命令")
    private String postCommand;
    @ApiModelProperty("删除命令")
    private String killCommand;
    @ApiModelProperty("删除命令是否存在")
    private Boolean killCommandExist;
    @ApiModelProperty("健康探针")
    private String healthProb;
    @ApiModelProperty("健康探针是否存在")
    private Boolean healthProbExist;
    @ApiModelProperty("应用是否准备好")
    private Boolean ready;
    @ApiModelProperty("docker容器的状态")
    private String status;
    @ApiModelProperty("docker容器的端口")
    private String ports;
    @ApiModelProperty
    private String hostStatus;
    @ApiModelProperty(value = "当前生效的配置id,为docker_compose部署类型时才需要")
    private Long effectValueId;
    @ApiModelProperty(value = "工作目录")
    private String workDir;

    public String getWorkDir() {
        return workDir;
    }

    public DevopsHostAppVO setWorkDir(String workDir) {
        this.workDir = workDir;
        return this;
    }

    public void decodeCommand() {
        this.preCommand = Base64Util.decodeBuffer(this.preCommand);
        this.runCommand = Base64Util.decodeBuffer(this.runCommand);
        this.postCommand = Base64Util.decodeBuffer(this.postCommand);
        this.healthProb = Base64Util.decodeBuffer(this.healthProb);
        this.killCommand = Base64Util.decodeBuffer(this.killCommand);
    }

    private DevopsDockerInstanceVO devopsDockerInstanceVO;

    private DockerComposeValueDTO dockerComposeValueDTO;

    private DevopsHostDTO devopsHostDTO;

    public DevopsHostDTO getDevopsHostDTO() {
        return devopsHostDTO;
    }

    public void setDevopsHostDTO(DevopsHostDTO devopsHostDTO) {
        this.devopsHostDTO = devopsHostDTO;
    }

    public Long getEffectValueId() {
        return effectValueId;
    }

    public void setEffectValueId(Long effectValueId) {
        this.effectValueId = effectValueId;
    }

    public DockerComposeValueDTO getDockerComposeValueDTO() {
        return dockerComposeValueDTO;
    }

    public void setDockerComposeValueDTO(DockerComposeValueDTO dockerComposeValueDTO) {
        this.dockerComposeValueDTO = dockerComposeValueDTO;
    }

    public DevopsDockerInstanceVO getDevopsDockerInstanceVO() {
        return devopsDockerInstanceVO;
    }

    public void setDevopsDockerInstanceVO(DevopsDockerInstanceVO devopsDockerInstanceVO) {
        this.devopsDockerInstanceVO = devopsDockerInstanceVO;
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

    /**
     * 部署对象id
     */
    private MarketDeployObjectInfoVO marketDeployObjectInfoVO;

    private ProdJarInfoVO prodJarInfoVO;

    private FileInfoVO fileInfoVO;

    private JarPullInfoDTO jarPullInfoDTO;

    @ApiModelProperty("groupId")
    private String groupId;
    @ApiModelProperty("artifactId")
    private String artifactId;
    @ApiModelProperty("version")
    private String version;

    @ApiModelProperty("中间件部署模式")
    private String middlewareMode;

    @ApiModelProperty("中间件部署版本")
    private String middlewareVersion;

    private Date creationDate;
    private Long createdBy;
    private Date lastUpdateDate;
    private Long lastUpdatedBy;
    private Long objectVersionNumber;

    private IamUserDTO creator;
    private IamUserDTO updater;


    @ApiModelProperty("操作命令")
    private DevopsHostCommandDTO devopsHostCommandDTO;

    public JarPullInfoDTO getJarPullInfoDTO() {
        return jarPullInfoDTO;
    }

    public void setJarPullInfoDTO(JarPullInfoDTO jarPullInfoDTO) {
        this.jarPullInfoDTO = jarPullInfoDTO;
    }

    public DevopsHostCommandDTO getDevopsHostCommandDTO() {
        return devopsHostCommandDTO;
    }

    public void setDevopsHostCommandDTO(DevopsHostCommandDTO devopsHostCommandDTO) {
        this.devopsHostCommandDTO = devopsHostCommandDTO;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public FileInfoVO getFileInfoVO() {
        return fileInfoVO;
    }

    public void setFileInfoVO(FileInfoVO fileInfoVO) {
        this.fileInfoVO = fileInfoVO;
    }

    public MarketDeployObjectInfoVO getMarketDeployObjectInfoVO() {
        return marketDeployObjectInfoVO;
    }

    public void setMarketDeployObjectInfoVO(MarketDeployObjectInfoVO marketDeployObjectInfoVO) {
        this.marketDeployObjectInfoVO = marketDeployObjectInfoVO;
    }

    public ProdJarInfoVO getProdJarInfoVO() {
        return prodJarInfoVO;
    }

    public void setProdJarInfoVO(ProdJarInfoVO prodJarInfoVO) {
        this.prodJarInfoVO = prodJarInfoVO;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public Long getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(Long lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
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

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getHostId() {
        return hostId;
    }

    public void setHostId(Long hostId) {
        this.hostId = hostId;
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

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getRdupmType() {
        return rdupmType;
    }

    public void setRdupmType(String rdupmType) {
        this.rdupmType = rdupmType;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getSourceConfig() {
        return sourceConfig;
    }

    public void setSourceConfig(String sourceConfig) {
        this.sourceConfig = sourceConfig;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeployWay() {
        return deployWay;
    }

    public void setDeployWay(String deployWay) {
        this.deployWay = deployWay;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public String getMiddlewareMode() {
        return middlewareMode;
    }

    public void setMiddlewareMode(String middlewareMode) {
        this.middlewareMode = middlewareMode;
    }

    public String getMiddlewareVersion() {
        return middlewareVersion;
    }

    public void setMiddlewareVersion(String middlewareVersion) {
        this.middlewareVersion = middlewareVersion;
    }

    public String getKillCommand() {
        return killCommand;
    }

    public void setKillCommand(String killCommand) {
        this.killCommand = killCommand;
    }

    public Boolean getKillCommandExist() {
        return killCommandExist;
    }

    public void setKillCommandExist(Boolean killCommandExist) {
        this.killCommandExist = killCommandExist;
    }

    public String getHealthProb() {
        return healthProb;
    }

    public void setHealthProb(String healthProb) {
        this.healthProb = healthProb;
    }

    public Boolean getReady() {
        return ready;
    }

    public void setReady(Boolean ready) {
        this.ready = ready;
    }

    public Boolean getHealthProbExist() {
        return healthProbExist;
    }

    public void setHealthProbExist(Boolean healthProbExist) {
        this.healthProbExist = healthProbExist;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPorts() {
        return ports;
    }

    public void setPorts(String ports) {
        this.ports = ports;
    }

    public String getHostStatus() {
        return hostStatus;
    }

    public void setHostStatus(String hostStatus) {
        this.hostStatus = hostStatus;
    }
}
