package io.choerodon.devops.infra.template;

import io.choerodon.core.notify.EmailTemplate;

/**
 * 应用服务创建失败的邮件模板
 *
 * @author zmf
 * @since 12/4/19
 */
//TODO @Component
public class AppServiceCreationFailureEmailTemplate implements EmailTemplate {

    @Override
    public String code() {
        return "AppServiceCreationFailureEmail";
    }

    @Override
    public String name() {
        return "应用服务创建失败邮件模板";
    }

    @Override
    public String businessTypeCode() {
        // TODO by zmf
        return null;
    }

    @Override
    public String title() {
        return "Choerodon通知";
    }

    /**
     * projectName, appServiceName
     */
    @Override
    public String content() {
        return "<p>您在项目“${projectName}”下创建的应用服务“${appServiceName}”失败</p>";
    }
}
