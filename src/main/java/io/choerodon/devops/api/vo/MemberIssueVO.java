package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author hao.wang@zknow.com
 * @since 2023-06-13 15:53:58
 */
public class MemberIssueVO {

    private String realName;
    private String imageUrl;
    private String email;
    @ApiModelProperty(value = "bug数")
    private Long bug;
    @ApiModelProperty(value = "代码异味数")
    private Long codeSmell;
    @ApiModelProperty(value = "漏洞数")
    private Long vulnerability;

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
