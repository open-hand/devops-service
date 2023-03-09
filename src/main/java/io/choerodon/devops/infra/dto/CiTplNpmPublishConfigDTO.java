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
 * 流水线模板npm发布配置(CiTplNpmPublishConfig)实体类
 *
 * @author hao.wang@zknow.com
 * @since 2023-01-09 17:46:47
 */

@ApiModel("流水线模板npm发布配置")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_ci_tpl_npm_publish_config")
public class CiTplNpmPublishConfigDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_CI_TEMPLATE_STEP_ID = "ciTemplateStepId";
    public static final String FIELD_NEXUS_REPO_ID = "nexusRepoId";
    private static final long serialVersionUID = -70907818633337202L;
    @Id
    @GeneratedValue
    @Encrypt
    private Long id;

    @ApiModelProperty(value = "所属步骤id", required = true)
    @Encrypt
    @NotNull
    private Long ciTemplateStepId;

    @ApiModelProperty(value = "nexus的maven仓库在制品库的主键id")
    @Encrypt
    private Long npmPushRepoId;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCiTemplateStepId() {
        return ciTemplateStepId;
    }

    public void setCiTemplateStepId(Long ciTemplateStepId) {
        this.ciTemplateStepId = ciTemplateStepId;
    }

    public Long getNpmPushRepoId() {
        return npmPushRepoId;
    }

    public void setNpmPushRepoId(Long npmPushRepoId) {
        this.npmPushRepoId = npmPushRepoId;
    }
}

