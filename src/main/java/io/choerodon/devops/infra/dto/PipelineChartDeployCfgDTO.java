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
 * chart部署任务配置表(PipelineChartDeployCfg)实体类
 *
 * @author
 * @since 2022-11-24 15:57:05
 */

@ApiModel("chart部署任务配置表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_pipeline_chart_deploy_cfg")
public class PipelineChartDeployCfgDTO extends AuditDomain {
    private static final long serialVersionUID = 463634980631338019L;

    public static final String FIELD_ID = "id";
    public static final String FIELD_PIPELINE_ID = "pipelineId";
    public static final String FIELD_ENV_ID = "envId";
    public static final String FIELD_APP_SERVICE_ID = "appServiceId";
    public static final String FIELD_VERSION = "version";
    public static final String FIELD_VALUE_ID = "valueId";
    public static final String FIELD_DEPLOY_TYPE = "deployType";
    public static final String FIELD_SKIP_CHECK_PERMISSION = "skipCheckPermission";
    public static final String FIELD_APP_ID = "appId";
    public static final String FIELD_APP_NAME = "appName";
    public static final String FIELD_APP_CODE = "appCode";

    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "流水线id", required = true)
    @NotNull
    private Long pipelineId;

    @ApiModelProperty(value = "环境Id,devops_env.id", required = true)
    @NotNull
    private Long envId;

    @ApiModelProperty(value = "应用服务Id,devops_app_service.id", required = true)
    @NotNull
    private Long appServiceId;

    @ApiModelProperty(value = "部署版本类型")
    private String version;

    @ApiModelProperty(value = "valueId,devops_deploy_value.id", required = true)
    @NotNull
    private Long valueId;

    @ApiModelProperty(value = "部署类型：create 新建实例， update 替换实例", required = true)
    @NotBlank
    private String deployType;

    @ApiModelProperty(value = "是否校验环境权限", required = true)
    @NotNull
    private Boolean skipCheckPermission;

    @ApiModelProperty(value = "应用id,devops_deploy_app_center_env.id")
    private Long appId;

    @ApiModelProperty(value = "应用名称,devops_deploy_app_center_env.name")
    private String appName;

    @ApiModelProperty(value = "应用编码,devops_deploy_app_center_env.code")
    private String appCode;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
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

