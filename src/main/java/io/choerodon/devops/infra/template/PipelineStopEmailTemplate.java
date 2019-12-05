package io.choerodon.devops.infra.template;

import io.choerodon.core.notify.EmailTemplate;
import io.choerodon.devops.infra.enums.PipelineNoticeType;

/**
 * @author zmf
 * @since 12/5/19
 */
// TODO by zmf
public class PipelineStopEmailTemplate implements EmailTemplate {
    @Override
    public String code() {
        return "PipelineStopEmail";
    }

    @Override
    public String name() {
        return "流水线人工审核被终止邮件模板";
    }

    @Override
    public String businessTypeCode() {
        return PipelineNoticeType.PIPELINESTOP.toValue();
    }

    @Override
    public String title() {
        return "Choerodon通知";
    }

    @Override
    public String content() {
        return "<p>项目“${projectName}”中的流水线“${pipelineName}”在阶段【${stageName}】被 ${auditName}:${realName} 终止</p>";
    }
}
