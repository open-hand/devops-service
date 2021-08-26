package io.choerodon.devops.infra.dto.market;

import java.util.Date;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * @author zmf
 * @since 2020/11/27
 */
@ModifyAudit
@VersionAudit
@Table(name = "MARKET_APP_VERSION")
public class MarketAppVersionDTO extends AuditDomain {
    @ApiModelProperty("自增主键")
    @Encrypt
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty("项目id")
    private Long projectId;
    @ApiModelProperty("所属应用id")
    private Long marketAppId;

    @ApiModelProperty("应用版本号（30字符）")
    private String versionNumber;

    @ApiModelProperty("接受通知人员id")
    private String notifyUserIds;

    @ApiModelProperty("版本描述（200字符）")
    private String introduction;

    @ApiModelProperty("版本文档（512字符）")
    private String documentUrl;

    @ApiModelProperty("备注 选填 （150字符）")
    private String comments;

    @ApiModelProperty("应用版本状态")
    private String status;

    @ApiModelProperty(name = "第一次发布应用版本的时间")
    private Date releaseDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getMarketAppId() {
        return marketAppId;
    }

    public void setMarketAppId(Long marketAppId) {
        this.marketAppId = marketAppId;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getNotifyUserIds() {
        return notifyUserIds;
    }

    public void setNotifyUserIds(String notifyUserIds) {
        this.notifyUserIds = notifyUserIds;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getDocumentUrl() {
        return documentUrl;
    }

    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }
}
