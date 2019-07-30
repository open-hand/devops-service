package io.choerodon.devops.api.vo;

import java.util.Date;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:50 2019/7/29
 * Description:
 */
public class MarketAppPublishVersionVO {
    private Long id;

    private Long appServiceId;

    @ApiModelProperty("应用的版本的ID")
    @NotNull(message = "error.versionId.cannot.be.null")
    private Long versionId;

    @ApiModelProperty("应用的版本")
    @NotNull(message = "error.version.cannot.be.null")
    @Size(max = 64, message = "error.version.size")
    private String version;

    @ApiModelProperty("版本日志")
    private String changelog;

    @ApiModelProperty("文档")
    private String document;

    @ApiModelProperty("审批状态，默认审批中doing, success, failed")
    @Size(max = 20, message = "error.approveStatus.size")
    private String approveStatus;

    @ApiModelProperty("审批返回信息")
    @Size(max = 255, message = "error.approveMessage.size")
    private String approveMessage;

    @ApiModelProperty("版本创建时间")
    @NotNull(message = "error.versionCreationDate.cannot.be.null")
    private Date versionCreationDate;
    @ApiModelProperty("版本发布时间")
    private Date publishDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getChangelog() {
        return changelog;
    }

    public void setChangelog(String changelog) {
        this.changelog = changelog;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
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

    public Date getVersionCreationDate() {
        return versionCreationDate;
    }

    public void setVersionCreationDate(Date versionCreationDate) {
        this.versionCreationDate = versionCreationDate;
    }

    public Date getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }
}
