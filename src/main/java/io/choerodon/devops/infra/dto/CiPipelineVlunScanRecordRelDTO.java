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
 * ci流水线漏洞扫描记录关系表(CiPipelineVlunScanRecordRel)实体类
 *
 * @author hao.wang@zknow.com
 * @since 2023-05-31 15:27:24
 */

@ApiModel("ci流水线漏洞扫描记录关系表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_ci_pipeline_vlun_scan_record_rel")
public class CiPipelineVlunScanRecordRelDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_APP_SERVICE_ID = "appServiceId";
    public static final String FIELD_GITLAB_PIPELINE_ID = "gitlabPipelineId";
    public static final String FIELD_JOB_NAME = "jobName";
    public static final String FIELD_SCAN_RECORD_ID = "scanRecordId";
    private static final long serialVersionUID = -93753767554518043L;
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

    @ApiModelProperty(value = "devops_vuln_scan_record.id", required = true)
    @NotNull
    private Long scanRecordId;

    public CiPipelineVlunScanRecordRelDTO() {
    }

    public CiPipelineVlunScanRecordRelDTO(Long appServiceId, Long gitlabPipelineId, String jobName, Long scanRecordId) {
        this.appServiceId = appServiceId;
        this.gitlabPipelineId = gitlabPipelineId;
        this.jobName = jobName;
        this.scanRecordId = scanRecordId;
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

    public Long getScanRecordId() {
        return scanRecordId;
    }

    public void setScanRecordId(Long scanRecordId) {
        this.scanRecordId = scanRecordId;
    }

}

