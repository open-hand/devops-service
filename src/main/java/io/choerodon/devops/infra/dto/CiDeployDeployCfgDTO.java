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
 * CI deployment部署任务配置表(CiDeployDeployCfg)实体类
 *
 * @author hao.wang@zknow.com
 * @since 2022-11-07 10:15:31
 */

@ApiModel("CI deployment部署任务配置表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_ci_deploy_deploy_cfg")
public class CiDeployDeployCfgDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_ENV_ID = "envId";
    public static final String FIELD_DEPLOY_TYPE = "deployType";
    public static final String FIELD_SKIP_CHECK_PERMISSION = "skipCheckPermission";
    public static final String FIELD_APP_NAME = "appName";
    public static final String FIELD_APP_CODE = "appCode";
    public static final String FIELD_APP_CONFIG_JSON = "appConfigJson";
    public static final String FIELD_CONTAINER_CONFIG_JSON = "containerConfigJson";
    private static final long serialVersionUID = 692407374336909588L;
    @Id
    @GeneratedValue
    private Long id;
    @ApiModelProperty(value = "关联devops流水线id")
    private Long ciPipelineId;

    @ApiModelProperty(value = "环境Id,devops_env.id", required = true)
    @NotNull
    private Long envId;

    @ApiModelProperty(value = "部署类型：create 新建实例， update 替换实例", required = true)
    @NotBlank
    private String deployType;

    @ApiModelProperty(value = "是否校验环境权限", required = true)
    @NotNull
    private Object skipCheckPermission;
    @ApiModelProperty(value = "应用idId", required = true)
    @Encrypt
    private Long appId;
    @ApiModelProperty(value = "应用名称,devops_deploy_app_center_env.name", required = true)
    @NotBlank
    private String appName;

    @ApiModelProperty(value = "应用编码,devops_deploy_app_center_env.code", required = true)
    @NotBlank
    private String appCode;

    @ApiModelProperty(value = "应用配置", required = true)
    @NotBlank
    private String appConfigJson;

    @ApiModelProperty(value = "容器配置", required = true)
    @NotBlank
    private String containerConfigJson;

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

    public String getDeployType() {
        return deployType;
    }

    public void setDeployType(String deployType) {
        this.deployType = deployType;
    }

    public Object getSkipCheckPermission() {
        return skipCheckPermission;
    }

    public void setSkipCheckPermission(Object skipCheckPermission) {
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

    public String getAppConfigJson() {
        return appConfigJson;
    }

    public void setAppConfigJson(String appConfigJson) {
        this.appConfigJson = appConfigJson;
    }

    public String getContainerConfigJson() {
        return containerConfigJson;
    }

    public void setContainerConfigJson(String containerConfigJson) {
        this.containerConfigJson = containerConfigJson;
    }

}

