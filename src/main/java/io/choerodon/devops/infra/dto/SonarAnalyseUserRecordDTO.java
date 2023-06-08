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
 * 代码扫描记录表(SonarAnalyseUserRecord)实体类
 *
 * @author hao.wang@zknow.com
 * @since 2023-06-06 15:44:31
 */

@ApiModel("代码扫描记录表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_sonar_analyse_author_count")
public class SonarAnalyseUserRecordDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_RECORD_ID = "recordId";
    public static final String FIELD_USER_EMAIL = "userEmail";
    public static final String FIELD_BUG = "bug";
    public static final String FIELD_CODE_SMELL = "codeSmell";
    public static final String FIELD_VULNERABILITY = "vulnerability";
    private static final long serialVersionUID = 918864368250904668L;
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "devops_sonar_analyse_record.id", required = true)
    @NotNull
    private Long recordId;

    @NotBlank
    private String author;

    @ApiModelProperty(value = "bug数")
    private Long bug;

    @ApiModelProperty(value = "代码异味数")
    private Long codeSmell;

    @ApiModelProperty(value = "漏洞数")
    private Long vulnerability;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
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

}

