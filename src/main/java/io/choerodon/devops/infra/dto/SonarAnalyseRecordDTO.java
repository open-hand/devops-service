package io.choerodon.devops.infra.dto;

import java.util.Date;
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
 * 代码扫描记录表(SonarAnalyseRecord)实体类
 *
 * @author hao.wang@zknow.com
 * @since 2023-06-06 15:44:31
 */

@ApiModel("代码扫描记录表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_sonar_analyse_record")
public class SonarAnalyseRecordDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_PROJECT_ID = "projectId";
    public static final String FIELD_APP_SERVICE_ID = "appServiceId";
    public static final String FIELD_ANALYSED_AT = "analysedAt";
    public static final String FIELD_BUG = "bug";
    public static final String FIELD_CODE_SMELL = "codeSmell";
    public static final String FIELD_VULNERABILITY = "vulnerability";
    public static final String FIELD_SQALE_INDEX = "sqaleIndex";
    public static final String FIELD_QUALITY_GATE_DETAILS = "qualityGateDetails";
    public static final String FIELD_SCORE = "score";
    private static final long serialVersionUID = -23514771653325539L;
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "项目Id", required = true)
    @NotNull
    private Long projectId;

    @ApiModelProperty(value = "应用服务id", required = true)
    @NotNull
    private Long appServiceId;

    @ApiModelProperty(value = "扫描时间", required = true)
    @NotNull
    private Date analysedAt;

    @ApiModelProperty(value = "bug数")
    private Long bug;

    @ApiModelProperty(value = "代码异味数")
    private Long codeSmell;

    @ApiModelProperty(value = "漏洞数")
    private Long vulnerability;

    @ApiModelProperty(value = "技术债务")
    private Long sqaleIndex;

    @ApiModelProperty(value = "质量门详情")
    private String qualityGateDetails;

    @ApiModelProperty(value = "评分")
    private Long score;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Date getAnalysedAt() {
        return analysedAt;
    }

    public void setAnalysedAt(Date analysedAt) {
        this.analysedAt = analysedAt;
    }

    public Long getBug() {
        return bug;
    }

    public void setBug(Long bug) {
        this.bug = bug;
    }

    public Long getCodeSmell() {
        return codeSmell;
    }

    public void setCodeSmell(Long codeSmell) {
        this.codeSmell = codeSmell;
    }

    public Long getVulnerability() {
        return vulnerability;
    }

    public void setVulnerability(Long vulnerability) {
        this.vulnerability = vulnerability;
    }

    public Long getSqaleIndex() {
        return sqaleIndex;
    }

    public void setSqaleIndex(Long sqaleIndex) {
        this.sqaleIndex = sqaleIndex;
    }

    public String getQualityGateDetails() {
        return qualityGateDetails;
    }

    public void setQualityGateDetails(String qualityGateDetails) {
        this.qualityGateDetails = qualityGateDetails;
    }

    public Long getScore() {
        return score;
    }

    public void setScore(Long score) {
        this.score = score;
    }

}

