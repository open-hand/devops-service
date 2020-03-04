package io.choerodon.devops.api.vo.polaris;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author zmf
 * @since 2/18/20
 */
public class PolarisSummaryItemDetailVO {
    @ApiModelProperty("是否通过")
    private Boolean approved;
    @ApiModelProperty("重视程度 / ignore/ warning / error")
    private String severity;
    @ApiModelProperty("item的消息")
    private String message;
    @ApiModelProperty("item的code")
    private String type;

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
