package io.choerodon.devops.infra.template;

import org.springframework.stereotype.Component;

import io.choerodon.core.notify.*;
import io.choerodon.devops.infra.constant.NoticeCodeConstants;

/**
 * @author zmf
 * @since 12/5/19
 */
@NotifyBusinessType(code = NoticeCodeConstants.SERVICE_CREATION_FAILURE,
        name = "网络创建失败", level = Level.PROJECT,
        description = "网络创建失败通知", isManualRetry = true, categoryCode = "deployment-resources-notice",
        pmEnabledFlag = true,
        emailEnabledFlag = true,
        proPmEnabledFlag = true,
        notifyType = ServiceNotifyType.DEVOPS_NOTIFY,
        targetUserType = {TargetUserType.TARGET_USER_CREATOR})
@Component
public class ServiceFailurePmTemplate implements PmTemplate {
    @Override
    public String code() {
        return "ServiceFailurePm";
    }

    @Override
    public String name() {
        return "网络创建失败站内信模板";
    }

    @Override
    public String businessTypeCode() {
        return NoticeCodeConstants.SERVICE_CREATION_FAILURE;
    }

    @Override
    public String title() {
        return "网络创建失败";
    }

    @Override
    public String content() {
        return "<p>您在项目“${projectName}”下“${envName}”环境中创建的网络“${resourceName}”失败。</p>";
    }
}
