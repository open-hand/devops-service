package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/30 14:54
 */
@Table(name = "devops_ci_maven_build_config")
@ModifyAudit
@VersionAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class DevopsCiMavenBuildConfigDTO extends AuditDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Encrypt
    @ApiModelProperty("项目下已有的maven仓库id列表 json")
    private String nexusMavenRepoIdStr;

    @ApiModelProperty("表单填写的Maven的依赖仓库 json格式")
    private String repoStr;

    @ApiModelProperty("直接粘贴的maven的settings内容")
    private String mavenSettings;

    @ApiModelProperty("所属步骤id")
    private Long stepId;

    public Long getStepId() {
        return stepId;
    }

    public void setStepId(Long stepId) {
        this.stepId = stepId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNexusMavenRepoIdStr() {
        return nexusMavenRepoIdStr;
    }

    public void setNexusMavenRepoIdStr(String nexusMavenRepoIdStr) {
        this.nexusMavenRepoIdStr = nexusMavenRepoIdStr;
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
}
