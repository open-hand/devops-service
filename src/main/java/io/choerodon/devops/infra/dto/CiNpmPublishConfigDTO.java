package io.choerodon.devops.infra.dto;

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
 * 流水线npm发布配置(CiNpmPublishConfig)实体类
 *
 * @author hao.wang@zknow.com
 * @since 2023-01-09 15:26:47
 */

@ApiModel("流水线npm发布配置")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_ci_npm_publish_config")
public class CiNpmPublishConfigDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_STEP_ID = "stepId";
    public static final String FIELD_NEXUS_REPO_ID = "nexusRepoId";
    private static final long serialVersionUID = 420262500987080011L;
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "所属步骤id", required = true)
    @NotNull
    private Long stepId;

    @ApiModelProperty(value = "nexus的maven仓库在制品库的主键id")
    private Long npmRepoId;


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

    public Long getNpmRepoId() {
        return npmRepoId;
    }

    public void setNpmRepoId(Long npmRepoId) {
        this.npmRepoId = npmRepoId;
    }
}

