package io.choerodon.devops.infra.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 发布应用服务版本步骤生成的流水线记录信息(CiPipelineAppVersion)实体类
 *
 * @author hao.wang@zknow.com
 * @since 2022-07-14 16:01:29
 */

@ApiModel("发布应用服务版本步骤生成的流水线记录信息")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_ci_pipeline_app_version")
public class CiPipelineAppVersionDTO extends AuditDomain {
    private static final long serialVersionUID = -24864468876350576L;

    public static final String FIELD_ID = "id";
    public static final String FIELD_APP_SERVICE_ID = "appServiceId";
    public static final String FIELD_GITLAB_PIPELINE_ID = "gitlabPipelineId";
    public static final String FIELD_JOB_NAME = "jobName";
    public static final String FIELD_APP_SERVICE_VERSION_ID = "appServiceVersionId";

    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "应用服务id", required = true)
    @NotNull
    private Long appServiceId;

    @ApiModelProperty(value = "gitlabPipelineId", required = true)
    @NotNull
    private Long gitlabPipelineId;

    @ApiModelProperty(value = "任务名称", required = true)
    @NotBlank
    private String jobName;

    @ApiModelProperty(value = "关联应用服务版本id，devops_app_service_version.id", required = true)
    @NotNull
    private Long appServiceVersionId;

    public CiPipelineAppVersionDTO() {
    }

    public CiPipelineAppVersionDTO(@NotNull Long appServiceId, @NotNull Long gitlabPipelineId, @NotBlank String jobName, @NotNull Long appServiceVersionId) {
        this.appServiceId = appServiceId;
        this.gitlabPipelineId = gitlabPipelineId;
        this.jobName = jobName;
        this.appServiceVersionId = appServiceVersionId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Long getGitlabPipelineId() {
        return gitlabPipelineId;
    }

    public void setGitlabPipelineId(Long gitlabPipelineId) {
        this.gitlabPipelineId = gitlabPipelineId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public Long getAppServiceVersionId() {
        return appServiceVersionId;
    }

    public void setAppServiceVersionId(Long appServiceVersionId) {
        this.appServiceVersionId = appServiceVersionId;
    }

}

