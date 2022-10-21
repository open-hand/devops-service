package io.choerodon.devops.api.vo;

import java.util.Map;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;


/**
 * @author lihao
 * @date 2019-09-15 15:35
 */
public class DevopsConfigMapUpdateVO {
    @Encrypt
    @ApiModelProperty("配置映射id/必填")
    @NotNull(message = "{devops.id.nulll}")
    private Long id;

    @Encrypt
    @ApiModelProperty("应用服务id")
    private Long appServiceId;

    @Encrypt
    @ApiModelProperty("环境id/必填")
    @NotNull(message = "{devops.env.id.null}")
    private Long envId;
    @ApiModelProperty("配置名称")
    @NotBlank(message = "{devops.name.null}")
    private String name;
    @ApiModelProperty("配置描述/非必填")
    private String description;
    private String type;
    @ApiModelProperty("配置值/不能为null")
    @NotNull(message = "error.configMap.value.null")
    private Map<String, String> value;

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
