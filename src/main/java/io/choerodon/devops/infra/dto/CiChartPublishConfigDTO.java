package io.choerodon.devops.infra.dto;

import javax.persistence.Column;
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
 * 流水线chart发布配置(CiChartPublishConfig)实体类
 *
 * @author hao.wang@zknow.com
 * @since 2023-01-04 15:28:30
 */

@ApiModel("流水线chart发布配置")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_ci_chart_publish_config")
public class CiChartPublishConfigDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_STEP_ID = "stepId";
    public static final String FIELD_REPO_ID = "repoId";
    private static final long serialVersionUID = -56756448103662134L;
    @Id
    @GeneratedValue
    @Encrypt
    private Long id;

    @ApiModelProperty(value = "所属步骤id", required = true)
    @Encrypt
    @NotNull
    private Long stepId;

    @ApiModelProperty(value = "helm仓库id")
    @Encrypt
    private Long repoId;
    @ApiModelProperty(value = "是否使用默认仓库")
    @Column(name = "is_use_default_repo")
    private Boolean useDefaultRepo;

    public Boolean getUseDefaultRepo() {
        return useDefaultRepo;
    }

    public void setUseDefaultRepo(Boolean useDefaultRepo) {
        this.useDefaultRepo = useDefaultRepo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStepId() {
        return stepId;
    }

    public void setStepId(Long stepId) {
        this.stepId = stepId;
    }

    public Long getRepoId() {
        return repoId;
    }

    public void setRepoId(Long repoId) {
        this.repoId = repoId;
    }

}

