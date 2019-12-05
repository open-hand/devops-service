package io.choerodon.devops.infra.template;

import io.choerodon.core.notify.EmailTemplate;

/**
 * @author zmf
 * @since 12/5/19
 */
// TODO by zmf
public class InstanceFailureEmailTemplate implements EmailTemplate {
    @Override
    public String code() {
        return "InstanceFailureEmail";
    }

    @Override
    public String name() {
        return "实例创建失败邮件模板";
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
        return "<p>您在项目“${projectName}”下“${envName}”环境中创建的实例“${resourceName}”失败</p>";
    }
}
