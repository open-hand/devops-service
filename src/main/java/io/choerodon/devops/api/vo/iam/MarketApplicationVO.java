package io.choerodon.devops.api.vo.iam;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  15:04 2019/8/27
 * Description: 应用市场上传修复版本Payload使用
 */
public class MarketApplicationVO {

    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "租户")
    @NotNull(message = "error.organizationId.cannot.be.null")
    private Long organizationId;

    @ApiModelProperty(value = "应用名称")
    private String name;

    @ApiModelProperty(value = "应用编码")
    private String code;

    @ApiModelProperty(value = "图标url")
    @NotNull(message = "error.imgUrl.cannot.be.null")
    private String imageUrl;

    @ApiModelProperty(value = "描述")
    @Size(max = 128, message = "error.description.size")
    @NotNull(message = "error.description.cannot.be.null")
    private String description;

    @ApiModelProperty(value = "贡献者，一般为项目名或者组织名")
    @Size(max = 100, message = "error.contributor.size")
    @NotNull(message = "error.contributor.cannot.be.null")
    private String contributor;

    @ApiModelProperty("应用概览")
    @NotNull(message = "error.overview.cannot.be.null")
    private String overview;

    @ApiModelProperty("应用发布日期")
    private Date publishDate;

    @ApiModelProperty("最新版本号，仅用于应用详情显示")
    private String latestVersion;

    @ApiModelProperty("最新版本更新时间，仅用于应用详情显示")
    private Date latestVersionDate;

    @ApiModelProperty(value = "是否免费，默认 1， 表示免费。 0代表收费")
    @NotNull(message = "error.isFree.cannot.be.null")
    private Boolean free;

    @ApiModelProperty(value = "应用类别名")
    private String categoryName;

    @ApiModelProperty("发布类型")
    @Size(max = 50, message = "error.publishType.size")
    private String type;

    @ApiModelProperty("应用类型Id")
    @NotNull(message = "error.categoryId.cannot.be.null")
    private Long categoryId;

    private MarketApplicationVersionVO marketApplicationVersionVO;
    private String version;
    private String approveStatus;
    private String approveMessage;
    private Integer downCount;
    private Long marketVersionId;
    private String document;

    @ApiModelProperty(value = "乐观锁版本号")
    private Long objectVersionNumber;

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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContributor() {
        return contributor;
    }

    public void setContributor(String contributor) {
        this.contributor = contributor;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public Date getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }

    public Boolean getFree() {
        return free;
    }

    public void setFree(Boolean free) {
        this.free = free;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getApproveStatus() {
        return approveStatus;
    }

    public void setApproveStatus(String approveStatus) {
        this.approveStatus = approveStatus;
    }

    public String getApproveMessage() {
        return approveMessage;
    }

    public void setApproveMessage(String approveMessage) {
        this.approveMessage = approveMessage;
    }

    public Long getMarketVersionId() {
        return marketVersionId;
    }

    public void setMarketVersionId(Long marketVersionId) {
        this.marketVersionId = marketVersionId;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public MarketApplicationVersionVO getMarketApplicationVersionVO() {
        return marketApplicationVersionVO;
    }

    public void setMarketApplicationVersionVO(MarketApplicationVersionVO marketApplicationVersionVO) {
        this.marketApplicationVersionVO = marketApplicationVersionVO;
    }

    public Date getLatestVersionDate() {
        return latestVersionDate;
    }

    public void setLatestVersionDate(Date latestVersionDate) {
        this.latestVersionDate = latestVersionDate;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }

    public Integer getDownCount() {
        return downCount;
    }

    public void setDownCount(Integer downCount) {
        this.downCount = downCount;
    }
}
