package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * @Author: scp
 * @Description:
 * @Date: Created in 2021/9/2
 * @Modified By:
 */
public class CheckAppServiceCodeAndNameVO {
    @ApiModelProperty("true:校验通过，false:已经存在该code，null:传参为null，没有校验")
    private Boolean code;
    private Boolean name;

    public Boolean getCode() {
        return code;
    }

    public void setCode(Boolean code) {
        this.code = code;
    }

    public Boolean getName() {
        return name;
    }

    public void setName(Boolean name) {
        this.name = name;
    }
}
