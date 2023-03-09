package io.choerodon.devops.infra.dto;

import java.util.List;
import java.util.Set;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.vo.MavenRepoVO;
import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * devops_ci_template_maven_publish(CiTemplateMavenPublish)实体类
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-15 14:06:08
 */

@ApiModel("devops_ci_template_maven_publish")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_ci_template_maven_publish")
public class CiTemplateMavenPublishDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_REPO_STR = "repoStr";
    public static final String FIELD_MAVEN_SETTINGS = "mavenSettings";
    public static final String FIELD_NEXUS_REPO_ID = "nexusRepoId";
    public static final String FIELD_CI_TEMPLATE_STEP_ID = "ciTemplateStepId";
    private static final long serialVersionUID = -67796445138548357L;
    @Id
    @GeneratedValue
    @Encrypt
    private Long id;


    @ApiModelProperty(value = "表单填写的Maven的依赖仓库")
    private String repoStr;

    @ApiModelProperty(value = "直接粘贴的maven的settings内容")
    private String mavenSettings;

    @ApiModelProperty(value = "所属步骤Id", required = true)
    @NotNull
    @Encrypt
    private Long ciTemplateStepId;

    @ApiModelProperty("项目下已有的maven仓库id列表 json")
    private String nexusMavenRepoIdStr;

    @Encrypt
    @ApiModelProperty("项目下已有的maven仓库id列表")
    @Transient
    private Set<Long> nexusMavenRepoIds;

    @ApiModelProperty("发包的目的仓库信息 json格式")
    private String targetRepoStr;


    @Encrypt
    @ApiModelProperty("nexus的maven仓库在制品库的主键id")
    private Long nexusRepoId;
    @ApiModelProperty("表单填写的Maven的依赖仓库")
    @Transient
    private List<MavenRepoVO> repos;
    @ApiModelProperty("发包的目的仓库信息")
    @Transient
    private MavenRepoVO targetRepo;

    @ApiModelProperty("坐标来源类型")
    private String gavSourceType;

    private String pomLocation;

    private String groupId;

    private String artifactId;

    private String version;
    private String packaging;

    public String getGavSourceType() {
        return gavSourceType;
    }

    public void setGavSourceType(String gavSourceType) {
        this.gavSourceType = gavSourceType;
    }

    public String getPomLocation() {
        return pomLocation;
    }

    public void setPomLocation(String pomLocation) {
        this.pomLocation = pomLocation;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPackaging() {
        return packaging;
    }

    public void setPackaging(String packaging) {
        this.packaging = packaging;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getRepoStr() {
        return repoStr;
    }

    public void setRepoStr(String repoStr) {
        this.repoStr = repoStr;
    }

    public String getMavenSettings() {
        return mavenSettings;
    }

    public void setMavenSettings(String mavenSettings) {
        this.mavenSettings = mavenSettings;
    }

    public Long getCiTemplateStepId() {
        return ciTemplateStepId;
    }

    public void setCiTemplateStepId(Long ciTemplateStepId) {
        this.ciTemplateStepId = ciTemplateStepId;
    }

    public String getNexusMavenRepoIdStr() {
        return nexusMavenRepoIdStr;
    }

    public void setNexusMavenRepoIdStr(String nexusMavenRepoIdStr) {
        this.nexusMavenRepoIdStr = nexusMavenRepoIdStr;
    }

    public String getTargetRepoStr() {
        return targetRepoStr;
    }

    public void setTargetRepoStr(String targetRepoStr) {
        this.targetRepoStr = targetRepoStr;
    }

    public Long getNexusRepoId() {
        return nexusRepoId;
    }

    public void setNexusRepoId(Long nexusRepoId) {
        this.nexusRepoId = nexusRepoId;
    }

    public Set<Long> getNexusMavenRepoIds() {
        return nexusMavenRepoIds;
    }

    public void setNexusMavenRepoIds(Set<Long> nexusMavenRepoIds) {
        this.nexusMavenRepoIds = nexusMavenRepoIds;
    }

    public List<MavenRepoVO> getRepos() {
        return repos;
    }

    public void setRepos(List<MavenRepoVO> repos) {
        this.repos = repos;
    }

    public MavenRepoVO getTargetRepo() {
        return targetRepo;
    }

    public void setTargetRepo(MavenRepoVO targetRepo) {
        this.targetRepo = targetRepo;
    }
}

