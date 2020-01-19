package io.choerodon.devops.infra.template;

import org.springframework.stereotype.Component;

import io.choerodon.core.notify.*;
import io.choerodon.devops.infra.constant.NoticeCodeConstants;

/**
 * @author zmf
 * @since 12/4/19
 */
@NotifyBusinessType(code = NoticeCodeConstants.MERGE_REQUEST_CLOSED,
        name = "合并请求被关闭", level = Level.PROJECT,
        description = "合并请求被关闭通知", isManualRetry = true, categoryCode = "code-management-notice",
        pmEnabledFlag = true,
        emailEnabledFlag = true,
        proPmEnabledFlag = true,
        notifyType = ServiceNotifyType.DEVOPS_NOTIFY,
        targetUserType = {TargetUserType.TARGET_USER_CREATOR})
@Component
public class MergeRequestClosedPmTemplate implements PmTemplate {
    @Override
    public String code() {
        return "MergeRequestClosedPm";
    }

    @Override
    public String name() {
        return "关闭MergeRequest站内信模板";
    }

    @Override
    public String businessTypeCode() {
        return NoticeCodeConstants.MERGE_REQUEST_CLOSED;
    }

    @Override
    public String title() {
        return "合并请求";
    }

    /**
     * projectName appServiceName realName gitlabUrl organizationCode projectCode appServiceCode mergeRequestId
     */
    @Override
    public String content() {
        return "<p>您在项目“${projectName}”下应用服务“${appServiceName}”中提交的合并请求已被 ${realName} 关闭。</p>\n" +
                "<p><a href=${gitlabUrl}/${organizationCode}-${projectCode}/${appServiceCode}/merge_requests/${mergeRequestId}>查看详情</a></p>";
    }
}
