package io.choerodon.devops.infra.template;

import io.choerodon.core.notify.EmailTemplate;
import io.choerodon.devops.infra.enums.PipelineNoticeType;

/**
 * @author zmf
 * @since 12/5/19
 */
// TODO by zmf
public class PipelinePassEmailTemplate implements EmailTemplate {
    @Override
    public String code() {
        return "PipelinePassEmail";
    }

    @Override
    public String name() {
        return "流水线或签任务通过通知邮件模板";
    }

    @Override
    public String businessTypeCode() {
        return PipelineNoticeType.PIPELINEPASS.toValue();
    }

    @Override
    public String title() {
        return "Choerodon通知";
    }

    @Override
    public String content() {
        return "<p>项目“${projectName}”中的流水线“${pipelineName}”在阶段【${stageName}】的或签任务已被 ${auditName}:${realName} 审核</p>";
    }
}
