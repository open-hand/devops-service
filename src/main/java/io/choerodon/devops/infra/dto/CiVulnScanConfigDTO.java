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
 * ci 漏洞扫描配置信息表(CiVulnScanConfig)实体类
 *
 * @author hao.wang@zknow.com
 * @since 2023-05-31 09:37:10
 */

@ApiModel("ci 漏洞扫描配置信息表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_ci_vuln_scan_config")
public class CiVulnScanConfigDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_STEP_ID = "stepId";
    public static final String FIELD_PATH = "path";
    private static final long serialVersionUID = 393791023095268952L;
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "所属步骤id", required = true)
    @NotNull
    private Long stepId;

    @ApiModelProperty(value = "扫描目录或文件", required = true)
    @NotBlank
    private String path;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStepId() {
        return stepId;
    }

    public void setStepId(Long stepId) {
        this.stepId = stepId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}

