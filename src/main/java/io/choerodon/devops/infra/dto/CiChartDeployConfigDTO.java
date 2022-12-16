package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * CI chart部署任务配置表(CiChartDeployConfig)实体类
 *
 * @author hao.wang@zknow.com
 * @since 2022-11-04 14:45:36
 */

@ApiModel("CI chart部署任务配置表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_ci_chart_deploy_cfg")
public class CiChartDeployConfigDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_ENV_ID = "envId";
    public static final String FIELD_VALUE_ID = "valueId";
    public static final String FIELD_DEPLOY_TYPE = "deployType";
    public static final String FIELD_SKIP_CHECK_PERMISSION = "skipCheckPermission";
    public static final String FIELD_APP_NAME = "appName";
    public static final String FIELD_APP_CODE = "appCode";
    private static final long serialVersionUID = 310832479417371197L;
    @Id
    @GeneratedValue
    private Long id;
    @ApiModelProperty(value = "关联devops流水线id")
    private Long ciPipelineId;

    @ApiModelProperty(value = "应用idId", required = true)
    @Encrypt
    private Long appId;

    @ApiModelProperty(value = "环境Id,devops_env.id", required = true)
    @NotNull
    private Long envId;

    @ApiModelProperty(value = "valueId,devops_deploy_value.id", required = true)
    @NotNull
    private Long valueId;

    @ApiModelProperty(value = "部署类型：create 新建实例， update 替换实例", required = true)
    @NotBlank
    private String deployType;

    @ApiModelProperty(value = "是否校验环境权限", required = true)
    @NotNull
    private Boolean skipCheckPermission;

    @ApiModelProperty(value = "应用名称,devops_deploy_app_center_env.name", required = true)
    @NotBlank
    private String appName;

    @ApiModelProperty(value = "应用编码,devops_deploy_app_center_env.code", required = true)
    @NotBlank
    private String appCode;

    public Long getCiPipelineId() {
        return ciPipelineId;
    }

    public void setCiPipelineId(Long ciPipelineId) {
        this.ciPipelineId = ciPipelineId;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public Long getValueId() {
        return valueId;
    }

    public void setValueId(Long valueId) {
        this.valueId = valueId;
    }

    public String getDeployType() {
        return deployType;
    }

    public void setDeployType(String deployType) {
        this.deployType = deployType;
    }

    public Boolean getSkipCheckPermission() {
        return skipCheckPermission;
    }

    public void setSkipCheckPermission(Boolean skipCheckPermission) {
        this.skipCheckPermission = skipCheckPermission;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

}

