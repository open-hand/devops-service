package io.choerodon.devops.api.vo;

import java.util.Map;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;

public class MiddlewareRedisEnvDeployVO extends MarketInstanceCreationRequestVO {
    @ApiModelProperty("部署模式")
    private String mode;

    @ApiModelProperty("中间件版本")
    private String version;

    @ApiModelProperty("pvc名称")
    private String pvcName;

    @ApiModelProperty("密码")
    @Size(min = 6, max = 32, message = "{devops.middleware.redis.password.length}")
    @NotBlank(message = "{devops.middleware.redis.password.empty}")
    private String password;

    @ApiModelProperty("是否启用内核优化")
    private Boolean sysctlImage;

    @ApiModelProperty("pv的labels")
    private Map<String, String> pvLabels;

    @ApiModelProperty("哨兵模式节点数量")
    @Min(value = 3, message = "{devops.middleware.redis.sentinel.slave.count}")
    private Integer slaveCount;

    @ApiModelProperty("redis的配置参数")
    private Map<String, String> configuration;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPvcName() {
        return pvcName;
    }

    public void setPvcName(String pvcName) {
        this.pvcName = pvcName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getSysctlImage() {
        return sysctlImage;
    }

    public void setSysctlImage(Boolean sysctlImage) {
        this.sysctlImage = sysctlImage;
    }

    public Map<String, String> getPvLabels() {
        return pvLabels;
    }

    public void setPvLabels(Map<String, String> pvLabels) {
        this.pvLabels = pvLabels;
    }

    public Integer getSlaveCount() {
        return slaveCount;
    }

    public void setSlaveCount(Integer slaveCount) {
        this.slaveCount = slaveCount;
    }

    public Map<String, String> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Map<String, String> configuration) {
        this.configuration = configuration;
    }
}
