package io.choerodon.devops.infra.template;

import org.springframework.stereotype.Component;

import io.choerodon.core.notify.EmailTemplate;
import io.choerodon.devops.infra.constant.NoticeCodeConstants;

/**
 * @author zmf
 * @since 12/4/19
 */
@Component
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
        return NoticeCodeConstants.AUDIT_MERGE_REQUEST;
    }

    @Override
    public String title() {
        return "合并请求";
    }

    @Override
    public String content() {
        return "<p>项目“${projectName}”下应用服务“${appServiceName}”中 ${realName} 提交了合并请求，需要您进行审核</p>";
    }
}
