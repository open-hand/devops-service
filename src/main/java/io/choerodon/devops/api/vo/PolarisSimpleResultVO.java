package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author zmf
 * @since 2/19/20
 */
public class PolarisSimpleResultVO {
    @ApiModelProperty("是否有error级别的检测项")
    private Boolean hasErrors;
    @ApiModelProperty("json数据")
    private String detailJson;

    public Boolean getHasErrors() {
        return hasErrors;
    }

    public void setHasErrors(Boolean hasErrors) {
        this.hasErrors = hasErrors;
    }

    public String getDetailJson() {
        return detailJson;
    }

    public void setDetailJson(String detailJson) {
        this.detailJson = detailJson;
    }
}
