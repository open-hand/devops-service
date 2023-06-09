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
 * 代码扫描问题分级统计表(SonarAnalyseIssueSeverity)实体类
 *
 * @author hao.wang@zknow.com
 * @since 2023-06-09 09:34:44
 */

@ApiModel("代码扫描问题分级统计表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_sonar_analyse_issue_severity")
public class SonarAnalyseIssueSeverityDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_RECORD_ID = "recordId";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_BLOCKER = "blocker";
    public static final String FIELD_CRITICAL = "critical";
    public static final String FIELD_MAJOR = "major";
    public static final String FIELD_MINOR = "minor";
    public static final String FIELD_INFO = "info";
    private static final long serialVersionUID = -46410211152846288L;
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "devops_sonar_analyse_record.id", required = true)
    @NotNull
    private Long recordId;

    @ApiModelProperty(value = "分类", required = true)
    @NotBlank
    private String type;

    @ApiModelProperty(value = "blocker问题数")
    private Long blocker;

    @ApiModelProperty(value = "critical问题数")
    private Long critical;

    @ApiModelProperty(value = "major问题数")
    private Long major;

    @ApiModelProperty(value = "minor问题数")
    private Long minor;

    @ApiModelProperty(value = "info问题数")
    private Long info;


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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getBlocker() {
        return blocker;
    }

    public void setBlocker(Long blocker) {
        this.blocker = blocker;
    }

    public Long getCritical() {
        return critical;
    }

    public void setCritical(Long critical) {
        this.critical = critical;
    }

    public Long getMajor() {
        return major;
    }

    public void setMajor(Long major) {
        this.major = major;
    }

    public Long getMinor() {
        return minor;
    }

    public void setMinor(Long minor) {
        this.minor = minor;
    }

    public Long getInfo() {
        return info;
    }

    public void setInfo(Long info) {
        this.info = info;
    }

}

