package io.choerodon.devops.infra.dto;

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
 * 流水线模板 chart部署任务配置表(CiTplChartDeployCfg)实体类
 *
 * @author hao.wang@zknow.com
 * @since 2022-11-04 15:04:59
 */

@ApiModel("流水线模板 chart部署任务配置表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_ci_tpl_chart_deploy_cfg")
public class CiTplChartDeployCfgDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_DEPLOY_TYPE = "deployType";
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "是否校验环境权限", required = true)
    @NotNull
    private Boolean skipCheckPermission;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getSkipCheckPermission() {
        return skipCheckPermission;
    }

    public void setSkipCheckPermission(Boolean skipCheckPermission) {
        this.skipCheckPermission = skipCheckPermission;
    }
}

