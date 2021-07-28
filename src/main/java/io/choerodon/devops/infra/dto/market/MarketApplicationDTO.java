package io.choerodon.devops.infra.dto.market;

import java.util.Date;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.vo.market.MarketCategoryVO;

/**
 * Created by wangxiang on 2020/11/27
 */
public class MarketApplicationDTO {
    @ApiModelProperty("应用id")
    @Encrypt
    private Long id;

    @ApiModelProperty(name = "应用名称")
    private String name;

    @ApiModelProperty(name = "类型 common/middleware/hzero")
    private String type;

    @ApiModelProperty("应用当前状态")
    private String status;
    @ApiModelProperty("与应用关联的申请状态")
    private String auditStatus;
    @ApiModelProperty(name = "贡献者")
    private String contributor;

    @ApiModelProperty(name = "app图标")
    private String imageUrl;

    @ApiModelProperty(name = "简介")
    private String introduction;

    @ApiModelProperty(name = "功能描述")
    private String functionDescription;


    private Long projectId;


    @ApiModelProperty(name = "第一次上架这个应用的时间")
    private Date releaseDate;

    @ApiModelProperty(name = "上次跟新的日期 ：修改应用服务，修改版本，发布修复版本")
    private Date updateDate;

    @ApiModelProperty(name = "最新版本")
    private String version;

    @ApiModelProperty(name = "应用版本列表")
    private List<AppVersionDTO> appVersionVOS;

    private List<MarketCategoryVO> marketCategoryVOS;

    @ApiModelProperty(name = "部署导入次数")
    private Integer importTimes;

    @ApiModelProperty("审批拒绝信息")
    private String auditMsg;

    @ApiModelProperty(name = "最近一次处于审批中状态的请求id")
    @Encrypt
    private Long latestAuditingRequestId;

    /**
     * 是否订阅
     */
    private Boolean subscription;
    /**
     * 来源项目
     */
    private String sourceProject;

    private Date creationDate;

    private Date lastUpdateDate;

    public List<AppVersionDTO> getAppVersionVOS() {
        return appVersionVOS;
    }

    public void setAppVersionVOS(List<AppVersionDTO> appVersionVOS) {
        this.appVersionVOS = appVersionVOS;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getSourceProject() {
        return sourceProject;
    }

    public void setSourceProject(String sourceProject) {
        this.sourceProject = sourceProject;
    }

    public Boolean getSubscription() {
        return subscription;
    }

    public void setSubscription(Boolean subscription) {
        this.subscription = subscription;
    }

    public Integer getImportTimes() {
        return importTimes;
    }

    public void setImportTimes(Integer importTimes) {
        this.importTimes = importTimes;
    }

    public List<MarketCategoryVO> getMarketCategoryVOS() {
        return marketCategoryVOS;
    }

    public void setMarketCategoryVOS(List<MarketCategoryVO> marketCategoryVOS) {
        this.marketCategoryVOS = marketCategoryVOS;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getFunctionDescription() {
        return functionDescription;
    }

    public void setFunctionDescription(String functionDescription) {
        this.functionDescription = functionDescription;
    }


    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getContributor() {
        return contributor;
    }

    public void setContributor(String contributor) {
        this.contributor = contributor;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(String auditStatus) {
        this.auditStatus = auditStatus;
    }

    public String getAuditMsg() {
        return auditMsg;
    }

    public void setAuditMsg(String auditMsg) {
        this.auditMsg = auditMsg;
    }

    public Long getLatestAuditingRequestId() {
        return latestAuditingRequestId;
    }

    public void setLatestAuditingRequestId(Long latestAuditingRequestId) {
        this.latestAuditingRequestId = latestAuditingRequestId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
