package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

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
    private Long ciTemplateStepId;


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

}

