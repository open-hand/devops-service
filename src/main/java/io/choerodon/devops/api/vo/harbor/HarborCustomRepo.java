package io.choerodon.devops.api.vo.harbor;

import java.util.Set;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * 制品库-harbor自定义镜像仓库表
 *
 * @author mofei.li@hand-china.com 2020-06-02 09:51:58
 */
@ApiModel("制品库-harbor自定义镜像仓库表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class HarborCustomRepo extends AuditDomain {

    public static final String FIELD_ID = "id";

    public static final String FIELD_PROJECT_ID = "projectId";
    public static final String FIELD_ORGANIZATION_ID = "organizationId";

    public static final String FIELD_REPO_NAME = "repoName";
    public static final String FIELD_REPO_URL = "repoUrl";
    public static final String FIELD_LOGIN_NAME = "loginName";
    public static final String FIELD_PSW = "password";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_PUBLIC_FLAG = "publicFlag";
    public static final String FIELD_PROJECT_SHARE = "projectShare";
    public static final String FIELD_CREATION_DATE = "creationDate";
    public static final String FIELD_CREATED_BY = "createdBy";
    public static final String FIELD_LAST_UPDATED_BY = "lastUpdatedBy";
    public static final String FIELD_LAST_UPDATE_DATE = "lastUpdateDate";
    public static final String FIELD_LAST_UPDATE_LOGIN = "lastUpdateLogin";

    //
    // 业务方法(按public protected private顺序排列)
    // ------------------------------------------------------------------------------


    public HarborCustomRepo() {
    }

    public HarborCustomRepo(HarborCustomRepoDTO harborCustomRepoDTO) {
        this.id = harborCustomRepoDTO.getRepoId();
        this.projectId = harborCustomRepoDTO.getProjectId();
        this.organizationId = harborCustomRepoDTO.getOrganizationId();
        this.repoName = harborCustomRepoDTO.getRepoName();
        this.repoUrl = harborCustomRepoDTO.getRepoUrl();
        this.loginName = harborCustomRepoDTO.getRepoLoginName();
        this.password = harborCustomRepoDTO.getRepoPassword();
        this.email = harborCustomRepoDTO.getRepoEmail();
        this.description = harborCustomRepoDTO.getRepoDescription();
        this.publicFlag = harborCustomRepoDTO.getRepoPublicFlag();
        this.projectShare = harborCustomRepoDTO.getProjectShare();
        this.set_token(harborCustomRepoDTO.get_token());
    }


    //
    // 数据库字段
    // ------------------------------------------------------------------------------


    @Encrypt
    @ApiModelProperty("表ID，主键，供其他表做外键")
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "猪齿鱼项目ID")
    private Long projectId;

    @ApiModelProperty(value = "猪齿鱼组织ID")
    private Long organizationId;

    @ApiModelProperty(value = "自定义镜像仓库名称（harbor项目名）",required = true)
    @NotBlank
    private String repoName;
    @ApiModelProperty(value = "自定义镜像仓库地址",required = true)
    @NotBlank
    private String repoUrl;
    @ApiModelProperty(value = "登录名",required = true)
    @NotBlank
    private String loginName;
    @ApiModelProperty(value = "密码",required = true)
    @NotBlank
    private String password;
    @ApiModelProperty(value = "邮箱",required = true)
    @NotBlank
    private String email;
   @ApiModelProperty(value = "描述")
    private String description;
    @ApiModelProperty(value = "是否公开访问，默认false")
    private String publicFlag;
    @ApiModelProperty(value = "是否项目下共享，默认false")
    @NotBlank
    private String projectShare;
	//
    // 非数据库字段
    // ------------------------------------------------------------------------------

    @Encrypt
    @Transient
    @ApiModelProperty(value = "关联的应用服务ID")
    private Set<Long> appServiceIds;

	@Transient
    @ApiModelProperty(value = "项目编码")
    private String projectCode;
    @Transient
    @ApiModelProperty(value = "创建人图标")
    private String creatorImageUrl;
    @Transient
    @ApiModelProperty(value = "创建人登录名")
    private String creatorLoginName;
    @Transient
    @ApiModelProperty(value = "创建人名称")
    private String creatorRealName;
    //
    // getter/setter
    // ------------------------------------------------------------------------------


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

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPublicFlag() {
        return publicFlag;
    }

    public void setPublicFlag(String publicFlag) {
        this.publicFlag = publicFlag;
    }

    public String getProjectShare() {
        return projectShare;
    }

    public void setProjectShare(String projectShare) {
        this.projectShare = projectShare;
    }

    public Set<Long> getAppServiceIds() {
        return appServiceIds;
    }

    public void setAppServiceIds(Set<Long> appServiceIds) {
        this.appServiceIds = appServiceIds;
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
}
