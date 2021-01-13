package io.choerodon.devops.api.vo.pipeline;

import java.util.Set;

/**
 * 〈功能简述〉
 * 〈API测试任务， 告警设置VO〉
 *
 * @author wanghao
 * @since 2021/1/11 16:11
 */
public class WarningSettingVO {
    private Boolean enableWarningSetting;
    private Double performThreshold;
    private Set<Long> notifyUserIds;
    private Boolean sendEmail;
    private Boolean sendSiteMessage;

    public Boolean getEnableWarningSetting() {
        return enableWarningSetting;
    }

    public void setEnableWarningSetting(Boolean enableWarningSetting) {
        this.enableWarningSetting = enableWarningSetting;
    }

    public Double getPerformThreshold() {
        return performThreshold;
    }

    public void setPerformThreshold(Double performThreshold) {
        this.performThreshold = performThreshold;
    }

    public Set<Long> getNotifyUserIds() {
        return notifyUserIds;
    }

    public void setNotifyUserIds(Set<Long> notifyUserIds) {
        this.notifyUserIds = notifyUserIds;
    }

    public Boolean getSendEmail() {
        return sendEmail;
    }

    public void setSendEmail(Boolean sendEmail) {
        this.sendEmail = sendEmail;
    }

    public Boolean getSendSiteMessage() {
        return sendSiteMessage;
    }

    public void setSendSiteMessage(Boolean sendSiteMessage) {
        this.sendSiteMessage = sendSiteMessage;
    }
}
