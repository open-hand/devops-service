package io.choerodon.devops.api.vo;

import java.util.Date;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.infra.dto.DevopsGitlabCommitDTO;

/**
 * Created by n!Ck
 * Date: 2018/9/19
 * Time: 17:18
 * Description:
 */
public class CommitFormRecordVO {
    @Encrypt
    @ApiModelProperty("用户id")
    private Long userId;
    @Encrypt
    @ApiParam("应用服务id")
    private Long appServiceId;
    @ApiParam("用户头像地址")
    private String imgUrl;
    @ApiParam("提交内容")
    private String commitContent;
    @ApiParam("用户名")
    private String userName;
    @ApiParam("提交日期")
    private Date commitDate;
    @ApiParam("commit sha")
    private String commitSHA;
    @ApiParam("应用服务名称")
    private String appServiceName;
    @ApiParam("应用服务编码")
    private String appServiceCode;
    @ApiParam("gitlab 地址")
    private String url;
    @ApiParam("是否是ldap用户")
    private Boolean ldap;
    @ApiParam("登录名")
    private String loginName;
    @ApiParam("邮箱")
    private String email;
    @ApiParam("真实姓名")
    private String realName;
    @ApiModelProperty("提交分支")
    private String ref;

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }


    public CommitFormRecordVO(Long userId, String imgUrl,
                              String userName,
                              DevopsGitlabCommitDTO devopsGitlabCommitDO) {
        this.userId = userId;
        this.appServiceId = devopsGitlabCommitDO.getAppServiceId();
        this.imgUrl = imgUrl;
        this.commitContent = devopsGitlabCommitDO.getCommitContent();
        this.userName = userName;
        this.commitDate = devopsGitlabCommitDO.getCommitDate();
        this.commitSHA = devopsGitlabCommitDO.getCommitSha();
        this.appServiceName = devopsGitlabCommitDO.getAppServiceName();
        this.url = devopsGitlabCommitDO.getUrl();
        this.ref=devopsGitlabCommitDO.getRef();
    }

    public CommitFormRecordVO(Long userId, DevopsGitlabCommitDTO devopsGitlabCommitDO, Boolean ldap, String loginName, String realName, String email, String imgUrl) {
        this.appServiceId = devopsGitlabCommitDO.getAppServiceId();
        this.commitContent = devopsGitlabCommitDO.getCommitContent();
        this.commitDate = devopsGitlabCommitDO.getCommitDate();
        this.commitSHA = devopsGitlabCommitDO.getCommitSha();
        this.appServiceName = devopsGitlabCommitDO.getAppServiceName();
        this.appServiceCode = devopsGitlabCommitDO.getAppServiceCode();
        this.url = devopsGitlabCommitDO.getUrl();
        this.userId = userId;
        this.ldap = ldap;
        this.loginName = loginName;
        this.realName = realName;
        this.email = email;
        this.imgUrl = imgUrl;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getCommitContent() {
        return commitContent;
    }

    public void setCommitContent(String commitContent) {
        this.commitContent = commitContent;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Date getCommitDate() {
        return commitDate;
    }

    public void setCommitDate(Date commitDate) {
        this.commitDate = commitDate;
    }

    public String getCommitSHA() {
        return commitSHA;
    }

    public void setCommitSHA(String commitSHA) {
        this.commitSHA = commitSHA;
    }

    public String getAppServiceName() {
        return appServiceName;
    }

    public void setAppServiceName(String appServiceName) {
        this.appServiceName = appServiceName;
    }

    public String getAppServiceCode() {
        return appServiceCode;
    }

    public void setAppServiceCode(String appServiceCode) {
        this.appServiceCode = appServiceCode;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getLdap() {
        return ldap;
    }

    public void setLdap(Boolean ldap) {
        this.ldap = ldap;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }
}
