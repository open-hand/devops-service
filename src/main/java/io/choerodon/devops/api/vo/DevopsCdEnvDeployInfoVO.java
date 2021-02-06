package io.choerodon.devops.api.vo;

import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/11 16:45
 */
public class DevopsCdEnvDeployInfoVO {
    @Encrypt
    private Long appServiceId;
    @Encrypt
    private Long envId;
    @Encrypt
    private Long valueId;
    private Long projectId;
    private String deployType;  // 部署类型：新建实例 create 替换实例 update
    @Encrypt
    private Long instanceId;    // 替换实例时需要
    private String instanceName;    // 新建实例时需要
    private String value;
    private Boolean checkEnvPermissionFlag;     // 部署时是否校验环境权限
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
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

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getDeployType() {
        return deployType;
    }

    public void setDeployType(String deployType) {
        this.deployType = deployType;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public Boolean getCheckEnvPermissionFlag() {
        return checkEnvPermissionFlag;
    }

    public void setCheckEnvPermissionFlag(Boolean checkEnvPermissionFlag) {
        this.checkEnvPermissionFlag = checkEnvPermissionFlag;
    }
}
