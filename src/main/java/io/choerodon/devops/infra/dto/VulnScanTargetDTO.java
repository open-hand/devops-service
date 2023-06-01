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
 * 漏洞扫描对象记录表(VulnScanTarget)实体类
 *
 * @author hao.wang@zknow.com
 * @since 2023-05-31 15:27:39
 */

@ApiModel("漏洞扫描对象记录表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_vuln_scan_target")
public class VulnScanTargetDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_SCAN_RECORD_ID = "scanRecordId";
    public static final String FIELD_TARGET = "target";
    private static final long serialVersionUID = 980534786203566171L;
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "devops_vuln_scan_record.id", required = true)
    @NotNull
    private Long scanRecordId;

    @ApiModelProperty(value = "扫描对象", required = true)
    @NotBlank
    private String target;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getScanRecordId() {
        return scanRecordId;
    }

    public void setScanRecordId(Long scanRecordId) {
        this.scanRecordId = scanRecordId;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

}

