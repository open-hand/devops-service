package io.choerodon.devops.api.dto;

import java.util.Map;

import io.swagger.annotations.ApiModelProperty;

/**
 * Created by n!Ck
 * Date: 18-12-4
 * Time: 上午9:46
 * Description:
 */
public class SecretReqDTO {
    @ApiModelProperty(value = "环境id/必填")
    private Long envId;

    @ApiModelProperty(value = "密钥名/必填")
    private String name;

    @ApiModelProperty(value = "密钥描述/非必填")
    private String description;

    @ApiModelProperty(value = "密钥对/必填")
    private Map<String, String> value;

    @ApiModelProperty(value = "创建或者更新")
    private String type;

    private Long ObjectVersionNumber;

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

    public Long getObjectVersionNumber() {
        return ObjectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        ObjectVersionNumber = objectVersionNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
