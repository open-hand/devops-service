package io.choerodon.devops.infra.template;

import io.choerodon.core.notify.EmailTemplate;

/**
 * @author zmf
 * @since 12/4/19
 */
// TODO by zmf
public class EnableAppServiceEmailTemplate implements EmailTemplate {
    @Override
    public String code() {
        return "EnableAppServiceEmail";
    }

    @Override
    public String name() {
        return "启用应用服务邮件模板";
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

    @Override
    public String content() {
        return "<p>项目“${projectName}”下的应用服务“${appServiceName}”已被启用</p>";
    }
}
