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
 * 流水线模板ci 漏洞扫描配置信息表(CiTplVulnScanConfig)实体类
 *
 * @author hao.wang@zknow.com
 * @since 2023-05-31 09:37:23
 */

@ApiModel("流水线模板ci 漏洞扫描配置信息表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_ci_tpl_vuln_scan_config")
public class CiTplVulnScanConfigDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_CI_TEMPLATE_STEP_ID = "ciTemplateStepId";
    public static final String FIELD_PATH = "path";
    private static final long serialVersionUID = -54209903856668805L;
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "所属步骤id", required = true)
    @NotNull
    private Long ciTemplateStepId;

    @ApiModelProperty(value = "扫描目录或文件", required = true)
    @NotBlank
    private String path;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCiTemplateStepId() {
        return ciTemplateStepId;
    }

    public void setCiTemplateStepId(Long ciTemplateStepId) {
        this.ciTemplateStepId = ciTemplateStepId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}

