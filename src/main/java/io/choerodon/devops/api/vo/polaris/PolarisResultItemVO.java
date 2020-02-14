package io.choerodon.devops.api.vo.polaris;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author zmf
 * @since 2/14/20
 */
public class PolarisResultItemVO {
    @ApiModelProperty("item的id")
    private String id;
    @ApiModelProperty("item没通过时的消息")
    private String message;
    @ApiModelProperty("是否通过")
    private Boolean success;
    @ApiModelProperty("重视程度 ignore/warning/error")
    private String severity;
    @ApiModelProperty("这个item的分类")
    private String category;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
