package io.choerodon.devops.infra.template;

import org.springframework.stereotype.Component;

import io.choerodon.core.notify.EmailTemplate;
import io.choerodon.devops.infra.constant.NoticeCodeConstants;

/**
 * @author zmf
 * @since 12/4/19
 */
@Component
public class GitLabContinuousDeliveryFailureEmailTemplate implements EmailTemplate {
    @Override
    public String code() {
        return "GitLabContinuousDeliveryFailureEmail";
    }

    @Override
    public String name() {
        return "GitLab持续集成失败邮件模板";
    }

    @Override
    public String businessTypeCode() {
        return NoticeCodeConstants.GITLAB_CONTINUOUS_DELIVERY_FAILURE;
    }

    @Override
    public String title() {
        return "Choerodon通知";
    }

    @Override
    public String content() {
        return "<p>您在项目“${projectName}”下应用服务“${appServiceName}”中的持续集成过程失败</p>";
    }
}
