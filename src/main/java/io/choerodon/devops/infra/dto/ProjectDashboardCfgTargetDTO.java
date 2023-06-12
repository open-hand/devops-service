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
 * 项目质量评分配置对象表(ProjectDashboardCfgTarget)实体类
 *
 * @author hao.wang@zknow.com
 * @since 2023-06-12 14:38:58
 */

@ApiModel("项目质量评分配置对象表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_project_dashboard_cfg_target")
public class ProjectDashboardCfgTargetDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_CFG_ID = "cfgId";
    public static final String FIELD_PROJECT_ID = "projectId";
    private static final long serialVersionUID = 411117877895500023L;
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "devops_project_dashboard_cfg.id", required = true)
    @NotNull
    private Long cfgId;

    @ApiModelProperty(value = "项目Id", required = true)
    @NotNull
    private Long projectId;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCfgId() {
        return cfgId;
    }

    public void setCfgId(Long cfgId) {
        this.cfgId = cfgId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

}

