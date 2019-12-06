package io.choerodon.devops.infra.template;

import org.springframework.stereotype.Component;

import io.choerodon.core.notify.*;
import io.choerodon.devops.infra.constant.NoticeCodeConstants;

/**
 * @author zmf
 * @since 12/4/19
 */
@NotifyBusinessType(code = NoticeCodeConstants.AUDIT_MERGE_REQUEST,
        name = "合并请求审核通知", level = Level.PROJECT,
        description = "合并请求审核通知", isAllowConfig = false, isManualRetry = true, categoryCode = "code-management-notice",
        pmEnabledFlag = true,
        emailEnabledFlag = true,
        notifyType = ServiceNotifyType.DEVOPS_NOTIFY,
        // 这个目标对象是不对的，但是前端不允许修改由后端发送时查询就暂时这样
        targetUserType = {TargetUserType.TARGET_USER_CREATOR})
@Component
public class AuditMergeRequestPmTemplate implements PmTemplate {
    @Override
    public String code() {
        return "AuditMergeRequestPm";
    }

    @Override
    public String name() {
        return "审核合并请求站内信模板";
    }

    @Override
    public String businessTypeCode() {
        return NoticeCodeConstants.AUDIT_MERGE_REQUEST;
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
        return "<p>项目“${projectName}”下应用服务“${appServiceName}”中${realName}提交了合并请求，需要您进行审核</p>\n" +
                "<p><a href=${gitlabUrl}/${organizationCode}-${projectCode}/${appServiceCode}/merge_requests/${mergeRequestId}>查看详情</a></p>";
    }
}
