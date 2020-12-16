package io.choerodon.devops.api.vo.market;

import io.swagger.annotations.ApiModelProperty;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * Created by wangxiang on 2020/12/16
 */
public class MarketAppUseRecordDTO {

    private Long id;

    @ApiModelProperty("使用组织或项目")
    private String userName;

    @ApiModelProperty("市场应用id")
    private String marketAppId;

    @ApiModelProperty("应用及版本")
    private String appAndVersion;

    @ApiModelProperty("应用服务及版本")
    private String appServiceAndVersion;

    @ApiModelProperty("应用来源")
    private String appServiceSource;

    @ApiModelProperty("应用贡献者（30字符）")
    private String contributor;

    @ApiModelProperty("用途")
    private String purpose;

    private Long appServiceId;

    /**
     *   应用服务版本id
     */
    private Long appServiceVersionId;

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Long getAppServiceVersionId() {
        return appServiceVersionId;
    }

    public void setAppServiceVersionId(Long appServiceVersionId) {
        this.appServiceVersionId = appServiceVersionId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMarketAppId() {
        return marketAppId;
    }

    public void setMarketAppId(String marketAppId) {
        this.marketAppId = marketAppId;
    }

    public String getAppAndVersion() {
        return appAndVersion;
    }

    public void setAppAndVersion(String appAndVersion) {
        this.appAndVersion = appAndVersion;
    }

    public String getAppServiceAndVersion() {
        return appServiceAndVersion;
    }

    public void setAppServiceAndVersion(String appServiceAndVersion) {
        this.appServiceAndVersion = appServiceAndVersion;
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
