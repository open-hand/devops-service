package io.choerodon.devops.infra.template;

import org.springframework.stereotype.Component;

import io.choerodon.core.notify.*;
import io.choerodon.devops.infra.constant.NoticeCodeConstants;

/**
 * @author zmf
 * @since 12/4/19
 */
@NotifyBusinessType(code = NoticeCodeConstants.INSTANCE_CREATION_FAILURE,
        name = "实例部署失败", level = Level.PROJECT,
        description = "实例部署失败通知", isAllowConfig = false, isManualRetry = true, categoryCode = "deployment-resources-notice",
        pmEnabledFlag = true,
        emailEnabledFlag = true,
        notifyType = ServiceNotifyType.DEVOPS_NOTIFY,
        targetUserType = {TargetUserType.TARGET_USER_INSTANCE_DEPLOYER})
@Component
public class InstanceFailurePmTemplate implements PmTemplate {
    @Override
    public String code() {
        return "InstanceFailurePm";
    }

    @Override
    public String name() {
        return "实例创建失败站内信模板";
    }

    @Override
    public String businessTypeCode() {
        return NoticeCodeConstants.INSTANCE_CREATION_FAILURE;
    }

    @Override
    public String title() {
        return "实例创建失败";
    }

    @Override
    public String content() {
        return "<p>您在项目“${projectName}”下“${envName}”环境中创建的实例“${resourceName}”失败</p>";
    }
}
