package io.choerodon.devops.api.vo.harbor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.mybatis.domian.SecurityToken;

import java.util.Date;

/**
 * description
 *
 * @author mofei.li@hand-china.com 2020/06/08 9:51
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel("制品库-自定义仓库DTO")
public class HarborCustomRepoDTO implements SecurityToken {
    @ApiModelProperty("customRepo, 主键")
    private Long repoId;
    @ApiModelProperty(value = "名称")
    private String repoName;
    @ApiModelProperty(value = "是否公开访问，默认false")
    private String repoPublicFlag;
    @ApiModelProperty(value = "地址")
    private String repoUrl;
    @ApiModelProperty(value = "仓库登录用户名")
    private String repoLoginName;
    @ApiModelProperty(value = "仓库登录密码")
    private String repoPassword;
    @ApiModelProperty(value = "仓库用户邮箱")
    private String repoEmail;
    @ApiModelProperty(value = "描述")
    private String repoDescription;
    @ApiModelProperty(value = "项目下共享")
    private String projectShare;


    @ApiModelProperty(value = "组织ID")
    private Long organizationId;
    @ApiModelProperty(value = "项目ID")
    private Long projectId;
    @ApiModelProperty(value = "项目编码")
    private String projectCode;
    @ApiModelProperty(value = "创建人图标")
    private String creatorImageUrl;
    @ApiModelProperty(value = "创建人登录名")
    private String creatorLoginName;
    @ApiModelProperty(value = "创建人名称")
    private String creatorRealName;

    private Long createdBy;
    private Date creationDate;
    private String _token;

    public HarborCustomRepoDTO() {
    }

    public HarborCustomRepoDTO(HarborCustomRepo harborCustomRepo) {
        this.repoId = harborCustomRepo.getId();
        this.repoName = harborCustomRepo.getRepoName();
        this.repoPublicFlag = harborCustomRepo.getPublicFlag();
        this.repoUrl = harborCustomRepo.getRepoUrl();
        this.repoLoginName = harborCustomRepo.getLoginName();
        this.repoPassword = harborCustomRepo.getPassword();
        this.repoEmail = harborCustomRepo.getEmail();
        this.repoDescription = harborCustomRepo.getDescription();
        this.projectShare = harborCustomRepo.getProjectShare();

        this.projectId = harborCustomRepo.getProjectId();
        this.organizationId = harborCustomRepo.getOrganizationId();
        this.projectCode = harborCustomRepo.getProjectCode();
        this.creatorImageUrl = harborCustomRepo.getCreatorImageUrl();
        this.creatorLoginName = harborCustomRepo.getCreatorLoginName();
        this.creatorRealName = harborCustomRepo.getCreatorRealName();

        this.createdBy = harborCustomRepo.getCreatedBy();
        this.creationDate = harborCustomRepo.getCreationDate();
        this._token = harborCustomRepo.get_token();
    }

    public Long getRepoId() {
        return repoId;
    }

    public void setRepoId(Long repoId) {
        this.repoId = repoId;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getRepoPublicFlag() {
        return repoPublicFlag;
    }

    public void setRepoPublicFlag(String repoPublicFlag) {
        this.repoPublicFlag = repoPublicFlag;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public String getRepoLoginName() {
        return repoLoginName;
    }

    public void setRepoLoginName(String repoLoginName) {
        this.repoLoginName = repoLoginName;
    }

    public String getRepoPassword() {
        return repoPassword;
    }

    public void setRepoPassword(String repoPassword) {
        this.repoPassword = repoPassword;
    }

    public String getRepoEmail() {
        return repoEmail;
    }

    public void setRepoEmail(String repoEmail) {
        this.repoEmail = repoEmail;
    }

    public String getRepoDescription() {
        return repoDescription;
    }

    public void setRepoDescription(String repoDescription) {
        this.repoDescription = repoDescription;
    }

    public String getProjectShare() {
        return projectShare;
    }

    public void setProjectShare(String projectShare) {
        this.projectShare = projectShare;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getCreatorImageUrl() {
        return creatorImageUrl;
    }

    public void setCreatorImageUrl(String creatorImageUrl) {
        this.creatorImageUrl = creatorImageUrl;
    }

    public String getCreatorLoginName() {
        return creatorLoginName;
    }

    public void setCreatorLoginName(String creatorLoginName) {
        this.creatorLoginName = creatorLoginName;
    }

    public String getCreatorRealName() {
        return creatorRealName;
    }

    public void setCreatorRealName(String creatorRealName) {
        this.creatorRealName = creatorRealName;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public String get_token() {
        return _token;
    }

    @Override
    public void set_token(String _token) {
        this._token = _token;
    }
}
