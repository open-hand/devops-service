package io.choerodon.devops.api.vo;

import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * Created by n!Ck
 * Date: 18-12-4
 * Time: 上午9:47
 * Description:
 */
public class SecretRespVO extends DevopsResourceDataInfoVO {
    @Encrypt
    private Long id;
    @ApiModelProperty("密文名称")
    private String name;
    @Encrypt
    @ApiModelProperty("环境id")
    private Long envId;
    @ApiModelProperty("描述")
    private String description;
    @ApiModelProperty("key list")
    private List<String> key;
    @ApiModelProperty("key value")
    private Map<String, String> value;
    @ApiModelProperty("command 状态")
    private String commandStatus;
    @ApiModelProperty("command 类型")
    private String commandType;
    @ApiModelProperty("错误信息")
    private String error;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getCommandStatus() {
        return commandStatus;
    }

    public void setCommandStatus(String commandStatus) {
        this.commandStatus = commandStatus;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
