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
 * 漏洞扫描对象关系表(VulnTargetRel)实体类
 *
 * @author hao.wang@zknow.com
 * @since 2023-05-31 15:27:39
 */

@ApiModel("漏洞扫描对象关系表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_vuln_target_rel")
public class VulnTargetRelDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_TARGET_ID = "targetId";
    public static final String FIELD_VULNERABILITY_ID = "vulnerabilityId";
    private static final long serialVersionUID = -80311856899343986L;
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "devops_vuln_scan_target.id", required = true)
    @NotNull
    private Long targetId;

    @ApiModelProperty(value = "漏洞id", required = true)
    @NotBlank
    private String vulnerabilityId;


    public VulnTargetRelDTO() {
    }

    public VulnTargetRelDTO(Long targetId, String vulnerabilityId) {
        this.targetId = targetId;
        this.vulnerabilityId = vulnerabilityId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getVulnerabilityId() {
        return vulnerabilityId;
    }

    public void setVulnerabilityId(String vulnerabilityId) {
        this.vulnerabilityId = vulnerabilityId;
    }

}

