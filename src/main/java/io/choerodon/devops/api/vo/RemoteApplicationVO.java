package io.choerodon.devops.api.vo;

import java.util.Date;

import io.swagger.annotations.ApiModelProperty;


/**
 * @author Eugen
 * 此 VO 用于 Paas平台 应用部署 的 远程应用市场
 */
public class RemoteApplicationVO {

    @ApiModelProperty(value = "主键")
    private Long id; // f:db 来自 数据库

    @ApiModelProperty(value = "应用ID")
    private Long appId; // f:db

    @ApiModelProperty(value = "应用名称")
    private String name; // f:db

    @ApiModelProperty(value = "应用编码")
    private String code; // f:db

    @ApiModelProperty(value = "图标url")
    private String imgUrl; // f:db

    @ApiModelProperty(value = "应用类别")
    private String category;  // f:db

    @ApiModelProperty(value = "描述")
    private String description; // f:db

    @ApiModelProperty(value = "贡献者，一般为项目名或者组织名")
    private String contributor; // f:db

    @ApiModelProperty(value = "是否免费，默认 1， 表示免费。 0代表收费")
    private Boolean free; // f:db

    @ApiModelProperty(value = "是否已购买，已购买为1")
    private Boolean bought; // f:c 来自代码计算

    @ApiModelProperty(value = "购买是否过期，过期为1")
    private Boolean expired; // f:c

    @ApiModelProperty(value = "部署次数")
    private Integer deployCount; //f:sql 来自sql计算

    @ApiModelProperty(value = "最近版本发布时间")
    private Date latestVersionDate; // f:db

    public Long getId() {
        return id;
    }

    public RemoteApplicationVO setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
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

    public String getImgUrl() {
        return imgUrl;
    }

    public RemoteApplicationVO setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public RemoteApplicationVO setCategory(String category) {
        this.category = category;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public RemoteApplicationVO setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getContributor() {
        return contributor;
    }

    public RemoteApplicationVO setContributor(String contributor) {
        this.contributor = contributor;
        return this;
    }

    public Boolean isFree() {
        return free;
    }

    public RemoteApplicationVO setFree(Boolean free) {
        this.free = free;
        return this;
    }

    public Boolean isBought() {
        return bought;
    }

    public RemoteApplicationVO setBought(Boolean bought) {
        this.bought = bought;
        return this;
    }

    public Boolean isExpired() {
        return expired;
    }

    public void setExpired(Boolean expired) {
        this.expired = expired;
    }

    public Integer getDeployCount() {
        return deployCount;
    }

    public void setDeployCount(Integer deployCount) {
        this.deployCount = deployCount;
    }

    public Date getLatestVersionDate() {
        return latestVersionDate;
    }

    public void setLatestVersionDate(Date latestVersionDate) {
        this.latestVersionDate = latestVersionDate;
    }
}
