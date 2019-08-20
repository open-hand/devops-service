package io.choerodon.devops.api.vo;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

/**
 * Created by younger on 2018/4/9.
 */
public class DevopsEnvironmentUpdateVO {
    @ApiModelProperty("环境id")
    @NotNull(message = "error.env.id.null")
    private Long id;
    @ApiModelProperty("环境名称，不能为空")
    @NotEmpty(message = "error.env.name.empty")
    private String name;
    @ApiModelProperty("环境描述")
    private String description;
    @ApiModelProperty("环境编码 / 不能修改")
    private String code;
    @ApiModelProperty("环境所属组id")
    private Long devopsEnvGroupId;
    @ApiModelProperty("版本号 / 更新必须")
    @NotNull(message = "error.object.version.number.null")
    private Long objectVersionNumber;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getDevopsEnvGroupId() {
        return devopsEnvGroupId;
    }

    public void setDevopsEnvGroupId(Long devopsEnvGroupId) {
        this.devopsEnvGroupId = devopsEnvGroupId;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }
}
