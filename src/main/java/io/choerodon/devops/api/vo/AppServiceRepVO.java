package io.choerodon.devops.api.vo;


import java.util.Date;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.vo.market.MarketServiceDeployObjectVO;
import io.choerodon.devops.infra.dto.AppExternalConfigDTO;
import io.choerodon.devops.infra.dto.harbor.HarborRepoConfigDTO;

/**
 * @author younger
 * @date 2018/3/30
 */
public class AppServiceRepVO {
    @ApiModelProperty("应用服务id")
    @Encrypt
    private Long id;

    @ApiModelProperty("应用服务名称")
    private String name;

    @ApiModelProperty("应用服务code")
    private String code;

    @ApiModelProperty("应用服务所属项目id")
    private Long projectId;

    @ApiModelProperty("应用服务对应gitlab项目的id")
    private Long gitlabProjectId;

    @ApiModelProperty("应用服务对应的gitlab仓库地址")
    private String repoUrl;

    @ApiModelProperty("应用服务对应的gitlab的仓库的ssh协议克隆地址")
    private String sshRepositoryUrl;

    @ApiModelProperty("应用服务是否同步完成，false表示正在处理中")
    private Boolean synchro;

    @ApiModelProperty("应用服务是否启用")
    private Boolean isActive;

    @Encrypt
    @ApiModelProperty("外部仓库配置id")
    private Long externalConfigId;

    @ApiModelProperty("应用服务描述")
    private String description;

    @ApiModelProperty("sonarqube地址")
    private String sonarUrl;

    @ApiModelProperty("应用服务是否失败，如果已同步且这个值为true说明应用服务创建失败")
    private Boolean fail;

    @ApiModelProperty("应用服务的类型")
    private String type;

    @ApiModelProperty("应用服务数据库纪录的版本号")
    private Long objectVersionNumber;


    @ApiModelProperty("应用服务对应的harbor配置信息")
    private HarborRepoConfigDTO harborRepoConfigDTO;

    @ApiModelProperty("应用服务对应的chart配置信息")
    private DevopsConfigVO chart;

    @ApiModelProperty("应用服务附加的pom信息：groupId（敏捷使用）")
    private String groupId;

    @ApiModelProperty("应用服务附加的pom信息：artifactId（敏捷使用）")
    private String artifactId;

    @ApiModelProperty("应用服务图标url")
    private String imgUrl;

    @ApiModelProperty("应用创建时间")
    private Date creationDate;

    @ApiModelProperty("应用服务最近更新时间")
    private Date lastUpdateDate;

    @ApiModelProperty("创建者用户名")
    private String createUserName;

    @ApiModelProperty("创建者登录名")
    private String createLoginName;

    @ApiModelProperty("创建者头像")
    private String createUserImage;

    @ApiModelProperty("最近更新者用户名")
    private String updateUserName;

    @ApiModelProperty("最近更新者登录名")
    private String updateLoginName;

    @ApiModelProperty("更新者头像")
    private String updateUserImage;

    @ApiModelProperty("是否是空仓库(是否没有分支)")
    private Boolean emptyRepository;

    @ApiModelProperty("应用服务类型")
    private String serviceType;

    @ApiModelProperty("来源项目名")
    private String shareProjectName;
    @ApiModelProperty("服务的编码")
    private String serviceCode;
    @ApiModelProperty("服务的名称")
    private String serviceName;
    @ApiModelProperty("服务来源")
    private String source;
    @ApiModelProperty("服务来源显示")
    private String sourceView;
    @ApiModelProperty("如果是市场服务的话是不是平台预置的")
    private Boolean builtIn;
    @ApiModelProperty("最新版本")
    private String latestVersion;

    @ApiModelProperty("事务实例id")
    @Encrypt
    private Long sagaInstanceId;
    @ApiModelProperty("错误信息")
    private String errorMessage;

    @ApiModelProperty("环境Id")
    @Encrypt
    private Long envId;

    @ApiModelProperty("如果是市场服务，对应的就是部署对象")
    private MarketServiceDeployObjectVO marketServiceDeployObjectVO;

    @ApiModelProperty("外部应用仓库配置")
    private AppExternalConfigDTO appExternalConfigDTO;

    @ApiModelProperty("用户权限等级")
    private Integer accessLevel;

    @ApiModelProperty("helm仓库配置id")
    @Encrypt
    private Long helmConfigId;

    private Double codeScore;

    private Double vulnScore;

    public Double getCodeScore() {
        return codeScore;
    }

    public void setCodeScore(Double codeScore) {
        this.codeScore = codeScore;
    }

    public Double getVulnScore() {
        return vulnScore;
    }

    public void setVulnScore(Double vulnScore) {
        this.vulnScore = vulnScore;
    }

    public Long getHelmConfigId() {
        return helmConfigId;
    }

    public void setHelmConfigId(Long helmConfigId) {
        this.helmConfigId = helmConfigId;
    }

    public Long getExternalConfigId() {
        return externalConfigId;
    }

    public void setExternalConfigId(Long externalConfigId) {
        this.externalConfigId = externalConfigId;
    }

    public AppExternalConfigDTO getAppExternalConfigDTO() {
        return appExternalConfigDTO;
    }

    public void setAppExternalConfigDTO(AppExternalConfigDTO appExternalConfigDTO) {
        this.appExternalConfigDTO = appExternalConfigDTO;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getSagaInstanceId() {
        return sagaInstanceId;
    }

    public void setSagaInstanceId(Long sagaInstanceId) {
        this.sagaInstanceId = sagaInstanceId;
    }

    public String getShareProjectName() {
        return shareProjectName;
    }

    public void setShareProjectName(String shareProjectName) {
        this.shareProjectName = shareProjectName;
    }

    public HarborRepoConfigDTO getHarborRepoConfigDTO() {
        return harborRepoConfigDTO;
    }

    public void setHarborRepoConfigDTO(HarborRepoConfigDTO harborRepoConfigDTO) {
        this.harborRepoConfigDTO = harborRepoConfigDTO;
    }

    public DevopsConfigVO getChart() {
        return chart;
    }

    public void setChart(DevopsConfigVO chart) {
        this.chart = chart;
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

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public Boolean getSynchro() {
        return synchro;
    }

    public void setSynchro(Boolean synchro) {
        this.synchro = synchro;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSonarUrl() {
        return sonarUrl;
    }

    public void setSonarUrl(String sonarUrl) {
        this.sonarUrl = sonarUrl;
    }

    public Boolean getFail() {
        return fail;
    }

    public void setFail(Boolean fail) {
        this.fail = fail;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public Long getGitlabProjectId() {
        return gitlabProjectId;
    }

    public void setGitlabProjectId(Long gitlabProjectId) {
        this.gitlabProjectId = gitlabProjectId;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getCreateUserName() {
        return createUserName;
    }

    public void setCreateUserName(String createUserName) {
        this.createUserName = createUserName;
    }

    public String getCreateLoginName() {
        return createLoginName;
    }

    public void setCreateLoginName(String createLoginName) {
        this.createLoginName = createLoginName;
    }

    public String getUpdateUserName() {
        return updateUserName;
    }

    public void setUpdateUserName(String updateUserName) {
        this.updateUserName = updateUserName;
    }

    public String getUpdateLoginName() {
        return updateLoginName;
    }

    public void setUpdateLoginName(String updateLoginName) {
        this.updateLoginName = updateLoginName;
    }

    public Boolean getEmptyRepository() {
        return emptyRepository;
    }

    public void setEmptyRepository(Boolean emptyRepository) {
        this.emptyRepository = emptyRepository;
    }

    public String getSshRepositoryUrl() {
        return sshRepositoryUrl;
    }

    public void setSshRepositoryUrl(String sshRepositoryUrl) {
        this.sshRepositoryUrl = sshRepositoryUrl;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getCreateUserImage() {
        return createUserImage;
    }

    public void setCreateUserImage(String createUserImage) {
        this.createUserImage = createUserImage;
    }

    public String getUpdateUserImage() {
        return updateUserImage;
    }

    public void setUpdateUserImage(String updateUserImage) {
        this.updateUserImage = updateUserImage;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public String getSourceView() {
        return sourceView;
    }

    public void setSourceView(String sourceView) {
        this.sourceView = sourceView;
    }

    public Boolean getBuiltIn() {
        return builtIn;
    }

    public void setBuiltIn(Boolean builtIn) {
        this.builtIn = builtIn;
    }

    public MarketServiceDeployObjectVO getMarketServiceDeployObjectVO() {
        return marketServiceDeployObjectVO;
    }

    public void setMarketServiceDeployObjectVO(MarketServiceDeployObjectVO marketServiceDeployObjectVO) {
        this.marketServiceDeployObjectVO = marketServiceDeployObjectVO;
    }

    public Integer getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(Integer accessLevel) {
        this.accessLevel = accessLevel;
    }
}

