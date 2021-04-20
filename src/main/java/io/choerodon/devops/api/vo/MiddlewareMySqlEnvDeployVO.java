package io.choerodon.devops.api.vo;

import java.util.Map;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;

public class MiddlewareMySqlEnvDeployVO extends MarketInstanceCreationRequestVO {
    @ApiModelProperty("部署模式")
    private String mode;

    @ApiModelProperty("中间件版本")
    private String version;

    @ApiModelProperty("pvc名称")
    private String pvcName;

    @ApiModelProperty("密码")
    @Size(min = 6, max = 32, message = "error.middleware.mysql.password.length")
    @NotBlank(message = "error.middleware.mysql.password.empty")
    private String password;

    @ApiModelProperty("MySQL的配置参数")
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

    public Map<String, String> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Map<String, String> configuration) {
        this.configuration = configuration;
    }
}
