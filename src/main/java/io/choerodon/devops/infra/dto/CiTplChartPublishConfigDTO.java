package io.choerodon.devops.infra.dto;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * 流水线模板chart发布配置(CiTplChartPublishConfig)实体类
 *
 * @author hao.wang@zknow.com
 * @since 2023-01-09 14:37:56
 */

@ApiModel("流水线模板chart发布配置")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_ci_tpl_chart_publish_config")
public class CiTplChartPublishConfigDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_CI_TEMPLATE_STEP_ID = "ciTemplateStepId";
    public static final String FIELD_IS_USE_DEFAULT_REPO = "isUseDefaultRepo";
    public static final String FIELD_REPO_ID = "repoId";
    private static final long serialVersionUID = -91354337278426319L;
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "所属步骤id", required = true)
    @NotNull
    private Long ciTemplateStepId;

    @ApiModelProperty(value = "是否使用默认仓库")
    @Column(name = "is_use_default_repo")
    private Boolean useDefaultRepo;

    @ApiModelProperty(value = "helm仓库id")
    private Long repoId;


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

    public Boolean getUseDefaultRepo() {
        return useDefaultRepo;
    }

    public void setUseDefaultRepo(Boolean useDefaultRepo) {
        this.useDefaultRepo = useDefaultRepo;
    }

    public Long getRepoId() {
        return repoId;
    }

    public void setRepoId(Long repoId) {
        this.repoId = repoId;
    }

}

