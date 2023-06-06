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
 * ci任务生成sonar记录(DevopsCiPipelineSonar)实体类
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-15 17:35:11
 */

@ApiModel("ci任务生成sonar记录")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_ci_pipeline_sonar")
public class DevopsCiPipelineSonarDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_GITLAB_PIPELINE_ID = "gitlabPipelineId";
    public static final String FIELD_JOB_NAME = "jobName";
    public static final String FIELD_SCANNER_TYPE = "scannerType";
    private static final long serialVersionUID = 996164540583995596L;
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty("应用服务id")
    private Long appServiceId;

    @ApiModelProperty(value = "gitlabPipelineId", required = true)
    @NotNull
    private Long gitlabPipelineId;

    private Long recordId;

    @ApiModelProperty(value = "任务名称", required = true)
    @NotBlank
    private String jobName;

    @ApiModelProperty(value = "sonar扫描器类型", required = true)
    @NotBlank
    private String scannerType;

    public DevopsCiPipelineSonarDTO() {
    }

    public DevopsCiPipelineSonarDTO(Long appServiceId, @NotNull Long gitlabPipelineId, @NotBlank String jobName) {
        this.appServiceId = appServiceId;
        this.gitlabPipelineId = gitlabPipelineId;
        this.jobName = jobName;
    }

    public DevopsCiPipelineSonarDTO(Long appServiceId, @NotNull Long gitlabPipelineId, @NotBlank String jobName, @NotBlank String scannerType) {
        this.appServiceId = appServiceId;
        this.gitlabPipelineId = gitlabPipelineId;
        this.jobName = jobName;
        this.scannerType = scannerType;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
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

    public String getScannerType() {
        return scannerType;
    }

    public void setScannerType(String scannerType) {
        this.scannerType = scannerType;
    }

}

