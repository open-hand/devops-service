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
        // TODO by zmf
        return null;
    }

    @Override
    public String name() {
        // TODO by zmf
        return null;
    }

    @Override
    public String businessTypeCode() {
        // TODO by zmf
        return null;
    }

    @Override
    public String title() {
        return null;
    }

    /**
     * projectCode, appServiceCode
     */
    @Override
    public String content() {
        return "<p>您在项目“${projectCode}”下创建的应用服务“${appServiceCode}”失败</p>";
    }
}
