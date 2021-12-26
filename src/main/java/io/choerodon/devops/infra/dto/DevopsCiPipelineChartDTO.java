package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * ci任务生成chart记录(DevopsCiPipelineChart)实体类
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-15 17:35:11
 */

@ApiModel("ci任务生成chart记录")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_ci_pipeline_chart")
public class DevopsCiPipelineChartDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_GITLAB_PIPELINE_ID = "gitlabPipelineId";
    public static final String FIELD_JOB_NAME = "jobName";
    public static final String FIELD_CHART_VERSION = "chartVersion";
    public static final String FIELD_APP_SERVICE_VERSION_ID = "appServiceVersionId";
    private static final long serialVersionUID = -40111900556657928L;
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty("应用服务id")
    private Long appServiceId;
    @ApiModelProperty(value = "gitlabPipelineId", required = true)
    @NotNull
    private Long gitlabPipelineId;

    @ApiModelProperty(value = "任务名称", required = true)
    @NotBlank
    private String jobName;

    @ApiModelProperty(value = "chart版本", required = true)
    @NotBlank
    private String chartVersion;

    @ApiModelProperty(value = "关联应用服务版本id", required = true)
    @NotNull
    private Long appServiceVersionId;


    public DevopsCiPipelineChartDTO() {
    }

    public DevopsCiPipelineChartDTO(@NotNull Long appServiceId,
                                    @NotNull Long gitlabPipelineId,
                                    @NotBlank String jobName,
                                    @NotBlank String chartVersion,
                                    @NotNull Long appServiceVersionId) {
        this.appServiceId = appServiceId;
        this.gitlabPipelineId = gitlabPipelineId;
        this.jobName = jobName;
        this.chartVersion = chartVersion;
        this.appServiceVersionId = appServiceVersionId;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getChartVersion() {
        return chartVersion;
    }

    public void setChartVersion(String chartVersion) {
        this.chartVersion = chartVersion;
    }

    public Long getAppServiceVersionId() {
        return appServiceVersionId;
    }

    public void setAppServiceVersionId(Long appServiceVersionId) {
        this.appServiceVersionId = appServiceVersionId;
    }

}

