package io.choerodon.devops.api.vo;

import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("应用服务版本和提交信息VO")
public class AppServiceVersionAndCommitVO {

    @ApiModelProperty("版本关联的gitlab commit sha")
    private String commit;
    @ApiModelProperty("版本关联的gitlab commit url")
    private String commitUrl;
    @ApiModelProperty("版本关联的gitlab commit 用户名")
    private String commitUserName;
    @ApiModelProperty("版本关联的gitlab commit 用户头像")
    private String commitUserImage;
    @ApiModelProperty("版本关联的gitlab commit 备注")
    private String commitContent;
    @ApiModelProperty("版本号")
    private String version;
    @ApiModelProperty("版本关联的应用服务名")
    private String appServiceName;
    @ApiModelProperty("版本的创建日期")
    private Date createDate;

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public String getCommitUrl() {
        return commitUrl;
    }

    public void setCommitUrl(String commitUrl) {
        this.commitUrl = commitUrl;
    }

    public String getCommitUserName() {
        return commitUserName;
    }

    public void setCommitUserName(String commitUserName) {
        this.commitUserName = commitUserName;
    }

    public String getCommitUserImage() {
        return commitUserImage;
    }

    public void setCommitUserImage(String commitUserImage) {
        this.commitUserImage = commitUserImage;
    }

    public String getCommitContent() {
        return commitContent;
    }

    public void setCommitContent(String commitContent) {
        this.commitContent = commitContent;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAppServiceName() {
        return appServiceName;
    }

    public void setAppServiceName(String appServiceName) {
        this.appServiceName = appServiceName;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
}
