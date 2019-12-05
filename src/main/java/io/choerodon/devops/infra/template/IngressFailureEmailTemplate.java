package io.choerodon.devops.infra.template;

import io.choerodon.core.notify.EmailTemplate;

/**
 * @author zmf
 * @since 12/5/19
 */
// TODO by zmf
public class IngressFailureEmailTemplate implements EmailTemplate {
    @Override
    public String code() {
        return "IngressFailureEmail";
    }

    @Override
    public String name() {
        return "创建域名失败邮件模板";
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

    @Override
    public String content() {
        return "<p>您在项目“${projectName}”下“${envName}”环境中创建的域名“${resourceCode}”失败</p>";
    }
}
