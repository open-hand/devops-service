package io.choerodon.devops.api.vo;

import java.util.Map;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * Created by n!Ck
 * Date: 18-12-4
 * Time: 上午9:46
 * Description:
 */
public class SecretReqVO {
    @Encrypt
    @ApiModelProperty(value = "密钥id")
    private Long id;

    @Encrypt
    @ApiModelProperty(value = "应用id/非必填")
    private Long appServiceId;

    @Encrypt
    @ApiModelProperty(value = "环境id/必填")
    @NotNull(message = "{devops.env.id.null}")
    private Long envId;

    @ApiModelProperty(value = "密钥名/必填")
    @NotBlank(message = "error.secret.name.null")
    private String name;

    @ApiModelProperty(value = "密钥对/必填")
    @NotNull(message = "error.secret.value.is.null")
    private Map<String, String> value;

    @ApiModelProperty(value = "密钥描述/非必填")
    private String description;

    @ApiModelProperty(value = "创建或者更新")
    private String type;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getValue() {
        return value;
    }

    public void setValue(Map<String, String> value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
