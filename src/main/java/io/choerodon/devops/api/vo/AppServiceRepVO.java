package io.choerodon.devops.api.vo;


import java.util.Date;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author younger
 * @date 2018/3/30
 */
public class AppServiceRepVO {
    @ApiModelProperty("应用服务id")
    private Long id;

    @ApiModelProperty("应用服务名称")
    private String name;

    @ApiModelProperty("应用服务code")
    private String code;

    @ApiModelProperty("应用服务所属应用id")
    private Long appId;

    @ApiModelProperty("应用服务对应gitlab项目的id")
    private Long gitlabProjectId;

    @ApiModelProperty("应用服务对应的gitlab仓库地址")
    private String repoUrl;

    @ApiModelProperty("应用服务是否同步完成，false表示正在处理中")
    private Boolean synchro;

    @ApiModelProperty("应用服务是否启用")
    private Boolean isActive;

    private String publishLevel;
    private String contributor;

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
    private DevopsConfigVO harbor;

    @ApiModelProperty("应用服务对应的chart配置信息")
    private DevopsConfigVO chart;

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

    @ApiModelProperty("最近更新者用户名")
    private String updateUserName;

    @ApiModelProperty("最近更新者登录名")
    private String updateLoginName;

    @ApiModelProperty("此应用服务是够跳过权限检查，true表示允许项目下所有的项目成员及项目所有者访问")
    private Boolean skipCheckPermission;

    public DevopsConfigVO getHarbor() {
        return harbor;
    }

    public void setHarbor(DevopsConfigVO harbor) {
        this.harbor = harbor;
    }

    public DevopsConfigVO getChart() {
        return chart;
    }

    public void setChart(DevopsConfigVO chart) {
        this.chart = chart;
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

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
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

    public String getPublishLevel() {
        return publishLevel;
    }

    public void setPublishLevel(String publishLevel) {
        this.publishLevel = publishLevel;
    }

    public String getContributor() {
        return contributor;
    }

    public void setContributor(String contributor) {
        this.contributor = contributor;
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

    public Boolean getSkipCheckPermission() {
        return skipCheckPermission;
    }

    public void setSkipCheckPermission(Boolean skipCheckPermission) {
        this.skipCheckPermission = skipCheckPermission;
    }
}
