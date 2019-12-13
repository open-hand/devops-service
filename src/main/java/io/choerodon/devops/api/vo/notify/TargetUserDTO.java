package io.choerodon.devops.api.vo.notify;

import io.swagger.annotations.ApiModelProperty;

/**
 * User: Mr.Wang
 * Date: 2019/12/13
 */
public class TargetUserDTO {

    private Long id;
    @ApiModelProperty("接收消息对象的类型")
    private String type;
    @ApiModelProperty("接收消息对象的id")
    private Long userId;
    @ApiModelProperty("平台设置表的id")
    private Long messageSettingId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getMessageSettingId() {
        return messageSettingId;
    }

    public void setMessageSettingId(Long messageSettingId) {
        this.messageSettingId = messageSettingId;
    }
}
