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
 * 单元测试报告(DevopsCiUnitTestReport)实体类
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-24 10:30:48
 */

@ApiModel("单元测试报告")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_ci_unit_test_report")
public class DevopsCiUnitTestReportDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_DEVOPS_PIPELINE_ID = "devopsPipelineId";
    public static final String FIELD_GITLAB_PIPELINE_ID = "gitlabPipelineId";
    public static final String FIELD_JOB_NAME = "jobName";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_TESTS = "tests";
    public static final String FIELD_FAILURES = "failures";
    public static final String FIELD_SKIPPED = "skipped";
    public static final String FIELD_REPORT_URL = "reportUrl";
    private static final long serialVersionUID = 695232608938282291L;
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

    @ApiModelProperty(value = "测试类型", required = true)
    @NotBlank
    private String type;

    @ApiModelProperty(value = "测试用例总数", required = true)
    @NotNull
    private Long tests;

    @ApiModelProperty(value = "失败用例总数", required = true)
    @NotNull
    private Long failures;

    @ApiModelProperty(value = "跳过用例总数", required = true)
    @NotNull
    private Long skipped;

    @ApiModelProperty(value = "测试报告地址")
    private String reportUrl;


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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getTests() {
        return tests;
    }

    public void setTests(Long tests) {
        this.tests = tests;
    }

    public Long getFailures() {
        return failures;
    }

    public void setFailures(Long failures) {
        this.failures = failures;
    }

    public Long getSkipped() {
        return skipped;
    }

    public void setSkipped(Long skipped) {
        this.skipped = skipped;
    }

    public String getReportUrl() {
        return reportUrl;
    }

    public void setReportUrl(String reportUrl) {
        this.reportUrl = reportUrl;
    }

}

