package io.choerodon.devops.infra.template;

import org.springframework.stereotype.Component;

import io.choerodon.core.notify.*;
import io.choerodon.devops.infra.constant.NoticeCodeConstants;

/**
 * 应用服务创建失败的站内信模板
 *
 * @author zmf
 * @since 12/4/19
 */
@NotifyBusinessType(code = NoticeCodeConstants.APP_SERVICE_CREATION_FAILED,
        name = "创建应用服务失败", level = Level.PROJECT,
        description = "创建应用服务失败通知", isManualRetry = true, categoryCode = "app-service-notice",
        pmEnabledFlag = true,
        emailEnabledFlag = true,
        proPmEnabledFlag = true,
        notifyType = ServiceNotifyType.DEVOPS_NOTIFY,
        targetUserType = {TargetUserType.TARGET_USER_CREATOR})
@Component
public class AppServiceCreationFailurePmTemplate implements PmTemplate {
    @Override
    public String code() {
        return "AppServiceCreationFailurePm";
    }

    @Override
    public String name() {
        return "应用服务创建失败站内信模板";
    }

    @Override
    public String businessTypeCode() {
        return NoticeCodeConstants.APP_SERVICE_CREATION_FAILED;
    }

    @Override
    public String title() {
        return "应用服务创建失败";
    }

    /**
     * 变量: projectName, appServiceName, projectId, projectCategory, organizationId
     */
    @Override
    public String content() {
        return "<p>您在项目“${projectName}”下创建的应用服务“${appServiceName}”失败</p>\n" +
                "<p><a href=#/devops/app-service?type=project&id=${projectId}&name=${appServiceName}&category=${projectCategory}&organizationId=${organizationId}>查看详情</a></p>";
    }
}
