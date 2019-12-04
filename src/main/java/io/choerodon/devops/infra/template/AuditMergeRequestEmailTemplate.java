package io.choerodon.devops.infra.template;

import io.choerodon.core.notify.EmailTemplate;

/**
 * @author zmf
 * @since 12/4/19
 */
// TODO by zmf
public class AuditMergeRequestEmailTemplate implements EmailTemplate {
    @Override
    public String code() {
        return "AuditMergeRequestEmail";
    }

    @Override
    public String name() {
        return "审核合并请求邮件模板";
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
        return "<p>项目“${projectName}”下应用服务“${appServiceName}”中 ${realName} 提交了合并请求，需要您进行审核</p>";
    }
}
