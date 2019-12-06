package io.choerodon.devops.infra.template;

import org.springframework.stereotype.Component;

import io.choerodon.core.notify.EmailTemplate;
import io.choerodon.devops.infra.constant.NoticeCodeConstants;

/**
 * @author zmf
 * @since 12/5/19
 */
@Component
public class ServiceFailureEmailTemplate implements EmailTemplate {
    @Override
    public String code() {
        return "ServiceFailureEmail";
    }

    @Override
    public String name() {
        return "网络失败邮件模板";
    }

    @Override
    public String businessTypeCode() {
        return NoticeCodeConstants.SERVICE_CREATION_FAILURE;
    }

    @Override
    public String title() {
        return "Choerodon通知";
    }

    @Override
    public String content() {
        return "<p>您在项目“${projectName}”下“${envName}”环境中创建的网络“${resourceName}”失败</p>";
    }
}
