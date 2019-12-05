package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * 〈功能简述〉
 * 〈资源删除验证VO〉
 *
 * @author wanghao
 * @Date 2019/12/4 15:58
 */
public class NotifyEventVO {
    @ApiModelProperty(value = "删除操作集合(envId,对应环境id)")
    private List<NotificationEventVO> devopsNotificationList = new ArrayList<>();
    @ApiModelProperty("项目下环境环境集合")
    private List<DevopsEnvironmentVO> devopsEnvironmentList = new ArrayList<>();

    public List<NotificationEventVO> getDevopsNotificationList() {
        return devopsNotificationList;
    }

    public void setDevopsNotificationList(List<NotificationEventVO> devopsNotificationList) {
        this.devopsNotificationList = devopsNotificationList;
    }

    public List<DevopsEnvironmentVO> getDevopsEnvironmentList() {
        return devopsEnvironmentList;
    }

    public void setDevopsEnvironmentList(List<DevopsEnvironmentVO> devopsEnvironmentList) {
        this.devopsEnvironmentList = devopsEnvironmentList;
    }

}
