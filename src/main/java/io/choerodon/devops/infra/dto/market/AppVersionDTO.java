package io.choerodon.devops.infra.dto.market;

import java.util.Date;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.vo.market.MarketServiceVO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/11/30 9:31
 */
public class AppVersionDTO {

    @Encrypt
    private Long id;

    @ApiModelProperty("所属应用的id")
    @Encrypt
    private Long marketAppId;

    @Size(message = "error.app.version.version.length")
    @NotNull(message = "error.app.version.version.null")
    @ApiModelProperty("应用版本号（30字符）")
    private String versionNumber;

    @Size(message = "error.app.version.introduction.length")
    @NotNull(message = "error.app.version.introduction.null")
    @ApiModelProperty("版本描述（200字符）")
    private String introduction;

    @Size(message = "error.app.version.document.url.length")
    @NotNull(message = "error.app.version.document.url.null")
    @ApiModelProperty("版本文档（400字符）")
    private String documentUrl;

    @ApiModelProperty("功能描述（200字符）")
    private String functionDescription;

    @ApiModelProperty("备注 选填 （150字符）")
    private String comments;

    private Date creationDate;
    private Long createdBy;
    private Date lastUpdateDate;
    private Long lastUpdatedBy;

    private Long objectVersionNumber;

    @NotEmpty(message = "error.app.version.service.null")
    @ApiModelProperty("这个应用版本所对应的市场服务")
    private List<MarketServiceVO> marketServiceVOList;

    @ApiModelProperty(name = "最近一次待审核的请求id，如果没有则为null")
    @Encrypt
    private Long latestAuditingRequestId;

    /**
     * 该版本的发布时间 已发布记录中的为准
     */
    private Date releaseDate;

    private String status;

    /**
     * 通知对象的id集合
     */
    private String notifyUserIds;

    /**
     * 负责人（通知对象）
     */
    private List<IamUserDTO> userVOS;

    @ApiModelProperty(name = "审核未通过的消息")
    private String auditMsg;

    private Long projectId;

    @ApiModelProperty("市场应用名称")
    private String marketAppName;

    @Encrypt
    @ApiModelProperty("失败的saga任务实例id/如果不为空则事务失败了，可以重试")
    private Long sagaTaskInstanceId;

    public String getMarketAppName() {
        return marketAppName;
    }

    public void setMarketAppName(String marketAppName) {
        this.marketAppName = marketAppName;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public List<IamUserDTO> getUserVOS() {
        return userVOS;
    }

    public void setUserVOS(List<IamUserDTO> userVOS) {
        this.userVOS = userVOS;
    }

    public String getNotifyUserIds() {
        return notifyUserIds;
    }

    public void setNotifyUserIds(String notifyUserIds) {
        this.notifyUserIds = notifyUserIds;
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

    public String getFunctionDescription() {
        return functionDescription;
    }

    public void setFunctionDescription(String functionDescription) {
        this.functionDescription = functionDescription;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public List<MarketServiceVO> getMarketServiceVOList() {
        return marketServiceVOList;
    }

    public void setMarketServiceVOList(List<MarketServiceVO> marketServiceVOList) {
        this.marketServiceVOList = marketServiceVOList;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getLatestAuditingRequestId() {
        return latestAuditingRequestId;
    }

    public void setLatestAuditingRequestId(Long latestAuditingRequestId) {
        this.latestAuditingRequestId = latestAuditingRequestId;
    }

    public String getAuditMsg() {
        return auditMsg;
    }

    public void setAuditMsg(String auditMsg) {
        this.auditMsg = auditMsg;
    }

    public Long getSagaTaskInstanceId() {
        return sagaTaskInstanceId;
    }

    public void setSagaTaskInstanceId(Long sagaTaskInstanceId) {
        this.sagaTaskInstanceId = sagaTaskInstanceId;
    }
}
