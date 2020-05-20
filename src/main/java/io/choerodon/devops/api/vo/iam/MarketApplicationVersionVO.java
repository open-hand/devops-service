package io.choerodon.devops.api.vo.iam;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;


/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  15:04 2019/8/27
 * Description: 应用市场上传修复版本Payload使用
 */
public class MarketApplicationVersionVO {
    private Long id;

    @ApiModelProperty("应用的Id")
    private String marketAppCode;


    @ApiModelProperty("应用的版本")
    private String version;

    @ApiModelProperty("版本日志")
    private String changelog;

    @ApiModelProperty("文档")
    private String document;

    @ApiModelProperty("审批状态，默认审批中doing, success, failed")
    private String approveStatus;

    @ApiModelProperty("审批返回信息")
    private String approveMessage;

    @ApiModelProperty("版本创建时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")

    private Date versionCreationDate;
    @ApiModelProperty("版本发布时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date publishDate;

    private List<MarketAppServiceVO> marketAppServiceVOS;

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

    public List<MarketAppServiceVO> getMarketAppServiceVOS() {
        return marketAppServiceVOS;
    }

    public void setMarketAppServiceVOS(List<MarketAppServiceVO> marketAppServiceVOS) {
        this.marketAppServiceVOS = marketAppServiceVOS;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMarketAppCode() {
        return marketAppCode;
    }

    public void setMarketAppCode(String marketAppCode) {
        this.marketAppCode = marketAppCode;
    }
}
