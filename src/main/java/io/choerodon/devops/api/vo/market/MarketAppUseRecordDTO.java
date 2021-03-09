package io.choerodon.devops.api.vo.market;

import io.swagger.annotations.ApiModelProperty;
import javax.persistence.*;
import org.hzero.starter.keyencrypt.core.Encrypt;


/**
 * Created by wangxiang on 2020/12/16
 */

public class MarketAppUseRecordDTO {

    @Id
    @Encrypt
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ApiModelProperty("使用组织或项目")
    private String userOrg;

    @ApiModelProperty("市场应用id")
    private Long marketAppId;

    @ApiModelProperty("部署人员的id，用于删除版本的时候通知订阅人员")
    private Long deployUserId;

    @ApiModelProperty("应用和版本（部署记录不应该随着删除版本而删除  所以需要记录）")
    private String appAndVersion;

    @ApiModelProperty("部署应用版本的id,用于删除版本的时候通知订阅人员")
    private Long marketAppVersionId;

    @ApiModelProperty("应用服务及版本")
    private String appServiceAndVersion;

    @ApiModelProperty("应用来源")
    private String appServiceSource;

    @ApiModelProperty("应用贡献者（30字符）")
    private String contributor;

    @ApiModelProperty("用途")
    private String purpose;

    @Transient
    private Long appServiceId;

    /**
     * 发布对象id
     */
    @Transient
    private Long deployObjectId;

    public String getAppServiceAndVersion() {
        return appServiceAndVersion;
    }

    public void setAppServiceAndVersion(String appServiceAndVersion) {
        this.appServiceAndVersion = appServiceAndVersion;
    }

    public String getUserOrg() {
        return userOrg;
    }

    public void setUserOrg(String userOrg) {
        this.userOrg = userOrg;
    }

    public Long getDeployUserId() {
        return deployUserId;
    }

    public void setDeployUserId(Long deployUserId) {
        this.deployUserId = deployUserId;
    }

    public Long getMarketAppVersionId() {
        return marketAppVersionId;
    }

    public void setMarketAppVersionId(Long marketAppVersionId) {
        this.marketAppVersionId = marketAppVersionId;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Long getDeployObjectId() {
        return deployObjectId;
    }

    public void setDeployObjectId(Long deployObjectId) {
        this.deployObjectId = deployObjectId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Long getMarketAppId() {
        return marketAppId;
    }

    public void setMarketAppId(Long marketAppId) {
        this.marketAppId = marketAppId;
    }

    public String getAppAndVersion() {
        return appAndVersion;
    }

    public void setAppAndVersion(String appAndVersion) {
        this.appAndVersion = appAndVersion;
    }


    public String getAppServiceSource() {
        return appServiceSource;
    }

    public void setAppServiceSource(String appServiceSource) {
        this.appServiceSource = appServiceSource;
    }

    public String getContributor() {
        return contributor;
    }

    public void setContributor(String contributor) {
        this.contributor = contributor;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
}
