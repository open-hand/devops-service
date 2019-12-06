package io.choerodon.devops.infra.template;

import org.springframework.stereotype.Component;

import io.choerodon.core.notify.EmailTemplate;
import io.choerodon.devops.infra.constant.NoticeCodeConstants;

/**
 * @author zmf
 * @since 12/4/19
 */
@Component
public class MergeRequestPassedEmailTemplate implements EmailTemplate {
    @Override
    public String code() {
        return "MergeRequestPassedEmail";
    }

    @Override
    public String name() {
        return "合并请求被通过邮件模板";
    }

    @Override
    public String businessTypeCode() {
        return NoticeCodeConstants.MERGE_REQUEST_PASSED;
    }

    @Override
    public String title() {
        return null;
    }

    @Override
    public String content() {
        return "<p>您在项目“${projectName}”下应用服务“${appServiceName}”中提交的合并请求已被 ${realName} 合并</p>";
    }
}
