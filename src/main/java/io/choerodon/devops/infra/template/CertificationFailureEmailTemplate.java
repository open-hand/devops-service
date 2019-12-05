package io.choerodon.devops.infra.template;

import io.choerodon.core.notify.EmailTemplate;

/**
 * @author zmf
 * @since 12/5/19
 */
// TODO by zmf
public class CertificationFailureEmailTemplate implements EmailTemplate {
    @Override
    public String code() {
        return "CertificationFailureEmail";
    }

    @Override
    public String name() {
        return "证书创建失败邮件模板";
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
        return "<p>您在项目“${projectName}”下“${envName}”环境中创建的证书“${resourceName}”失败</p>";
    }
}
