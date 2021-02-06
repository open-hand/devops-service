package io.choerodon.devops.api.vo.pipeline;

import io.swagger.annotations.ApiModelProperty;

/**
 * 〈功能简述〉
 * 〈外部卡点任务metadataVO〉
 *
 * @author wanghao
 * @since 2020/12/9 11:32
 */
public class ExternalApprovalJobVO {
    @ApiModelProperty("外部调用地址")
    private String triggerUrl;
    @ApiModelProperty("认证Token")
    private String secretToken;
    @ApiModelProperty("任务描述")
    private String description;


    public String getTriggerUrl() {
        return triggerUrl;
    }

    public void setTriggerUrl(String triggerUrl) {
        this.triggerUrl = triggerUrl;
    }

    public String getSecretToken() {
        return secretToken;
    }

    public void setSecretToken(String secretToken) {
        this.secretToken = secretToken;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "ExternalApprovalJobVO{" +
                "triggerUrl='" + triggerUrl + '\'' +
                ", secretToken='" + secretToken + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
