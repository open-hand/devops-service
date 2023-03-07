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
 * devops_ci_template_maven_build(CiTemplateMavenBuild)实体类
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-15 14:06:07
 */

@ApiModel("devops_ci_template_maven_build")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_ci_template_maven_build")
public class CiTemplateMavenBuildDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_NEXUS_MAVEN_REPO_ID_STR = "nexusMavenRepoIdStr";
    public static final String FIELD_REPO_STR = "repoStr";
    public static final String FIELD_MAVEN_SETTINGS = "mavenSettings";
    public static final String FIELD_CI_TEMPLATE_STEP_ID = "ciTemplateStepId";
    private static final long serialVersionUID = -42561927103880046L;
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

    @ApiModelProperty("表单填写的Maven的依赖仓库")
    @Transient
    private List<MavenRepoVO> repos;


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
}

