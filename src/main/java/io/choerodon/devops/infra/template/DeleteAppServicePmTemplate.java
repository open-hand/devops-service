package io.choerodon.devops.infra.template;

import io.choerodon.core.notify.*;
import io.choerodon.devops.infra.constant.NoticeCodeConstants;
import org.springframework.stereotype.Component;

/**
 * 删除应用服务的站内信模板
 *
 * @author zmf
 * @since 12/4/19
 */
@NotifyBusinessType(code = NoticeCodeConstants.DELETE_APP_SERVICE,
        name = "删除应用服务", level = Level.PROJECT,
        description = "删除应用服务通知", isManualRetry = true, categoryCode = "app-service-notice",
        pmEnabledFlag = true,
        emailEnabledFlag = true,
        proPmEnabledFlag = true,
        notifyType = ServiceNotifyType.DEVOPS_NOTIFY,
        targetUserType = {TargetUserType.TARGET_USER_APPLICATION_SERVICE_PERMISSION_OWNER})
@Component
public class DeleteAppServicePmTemplate implements PmTemplate {
    @Override
    public String code() {
        return "deleteAppService";
    }

    @Override
    public String name() {
        return "删除应用服务的站内信模板";
    }

    @Override
    public String businessTypeCode() {
        return NoticeCodeConstants.DELETE_APP_SERVICE;
    }

    @Override
    public String title() {
        return "应用服务已被删除";
    }

    /**
     * 变量: projectName, appServiceName, projectId, projectCategory, organizationId
     */
    @Override
    public String content() {
        return "<p>项目“${projectName}”下的应用服务“${appServiceName}”已被删除。查看详情</p>\n" +
                "<p><a href=#/devops/app-service?type=project&id=${projectId}&name=${appServiceName}&category=${projectCategory}&organizationId=${organizationId}>查看详情</a></p>";
    }
}
