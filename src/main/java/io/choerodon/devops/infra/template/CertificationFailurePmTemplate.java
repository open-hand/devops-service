package io.choerodon.devops.infra.template;

import org.springframework.stereotype.Component;

import io.choerodon.core.notify.*;

/**
 * @author zmf
 * @since 12/5/19
 */
@NotifyBusinessType(code = "certificationFailure", name = "证书创建失败", level = Level.PROJECT,
        description = "证书创建失败通知", isAllowConfig = false, isManualRetry = true, categoryCode = "deployment-resources-notice",
        pmEnabledFlag = true,
        notifyType = ServiceNotifyType.DEVOPS_NOTIFY,
        targetUserType = {TargetUserType.TARGET_USER_CREATOR})
@Component
public class CertificationFailurePmTemplate implements PmTemplate {
    @Override
    public String code() {
        return "CertificationFailurePm";
    }

    @Override
    public String name() {
        return "证书创建失败站内信模板";
    }

    @Override
    public String businessTypeCode() {
        // TODO by zmf
        return null;
    }

    @Override
    public String title() {
        return "证书创建失败";
    }

    @Override
    public String content() {
        return "<p>您在项目“${projectName}”下“${envName}”环境中创建的证书“${resourceName}”失败</p>";
    }
}
