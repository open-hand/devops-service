package io.choerodon.devops.api.vo.template;

import io.swagger.annotations.ApiModelProperty;

public class DeleteCheckResultVO {
    @ApiModelProperty("返回false表示被引用了")
    private Boolean result;

    @ApiModelProperty("层级")
    private String sourceType;

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }
}
