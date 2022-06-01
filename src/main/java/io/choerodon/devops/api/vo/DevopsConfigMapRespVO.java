package io.choerodon.devops.api.vo;

import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

public class DevopsConfigMapRespVO extends DevopsResourceDataInfoVO {
    @Encrypt
    private Long id;
    @Encrypt
    @ApiModelProperty("环境id")
    private Long envId;
    @Encrypt
    @ApiModelProperty("commandId")
    private Long commandId;
    @ApiModelProperty("command状态")
    private String commandStatus;
    @ApiModelProperty("环境编码")
    private String envCode;
    @ApiModelProperty("command类型")
    private String commandType;
    @ApiModelProperty("错误信息")
    private String error;
    @ApiModelProperty("环境状态")
    private Boolean envStatus;
    @ApiModelProperty("configmap 名称")
    private String name;
    @ApiModelProperty("keys")
    private List<String> key;
    @ApiModelProperty("values")
    private Map<String, String> value;
    @ApiModelProperty("configmap 描述")
    private String description;

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

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
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

    public String getCommandStatus() {
        return commandStatus;
    }

    public void setCommandStatus(String commandStatus) {
        this.commandStatus = commandStatus;
    }

    public List<String> getKey() {
        return key;
    }

    public void setKey(List<String> key) {
        this.key = key;
    }

    public Map<String, String> getValue() {
        return value;
    }

    public void setValue(Map<String, String> value) {
        this.value = value;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public Boolean getEnvStatus() {
        return envStatus;
    }

    public void setEnvStatus(Boolean envStatus) {
        this.envStatus = envStatus;
    }

    public String getEnvCode() {
        return envCode;
    }

    public void setEnvCode(String envCode) {
        this.envCode = envCode;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
