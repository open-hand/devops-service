package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author hao.wang@zknow.com
 * @since 2023-06-13 16:22:21
 */
public class SonarAnalyseIssueAuthorVO {


    private Long id;
    @ApiModelProperty(value = "devops_sonar_analyse_record.id", required = true)
    private Long recordId;

    private String author;
    @ApiModelProperty(value = "bug数")
    private Long bug;
    @ApiModelProperty(value = "代码异味数")
    private Long codeSmell;
    @ApiModelProperty(value = "漏洞数")
    private Long vulnerability;

    private String realName;
    private String imageUrl;
    private String email;

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Long getBug() {
        return bug;
    }

    public void setBug(Long bug) {
        this.bug = bug;
    }

    public Long getCodeSmell() {
        return codeSmell;
    }

    public void setCodeSmell(Long codeSmell) {
        this.codeSmell = codeSmell;
    }

    public Long getVulnerability() {
        return vulnerability;
    }

    public void setVulnerability(Long vulnerability) {
        this.vulnerability = vulnerability;
    }
}
