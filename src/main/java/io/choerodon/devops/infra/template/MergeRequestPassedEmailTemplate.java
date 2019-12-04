package io.choerodon.devops.infra.template;

import io.choerodon.core.notify.EmailTemplate;

/**
 * @author zmf
 * @since 12/4/19
 */
// TODO by zmf
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
        // TODO by zmf
        return null;
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
