package io.choerodon.devops.infra.template;

import org.springframework.stereotype.Component;

import io.choerodon.core.notify.EmailTemplate;
import io.choerodon.devops.infra.enums.PipelineNoticeType;

/**
 * @author zmf
 * @since 12/5/19
 */
@Component
public class PipelineSuccessEmailTemplate implements EmailTemplate {
    @Override
    public String code() {
        return "PipelineSuccessEmail";
    }

    @Override
    public String name() {
        return "流水线成功邮件模板";
    }

    @Override
    public String businessTypeCode() {
        return PipelineNoticeType.PIPELINESUCCESS.toValue();
    }

    @Override
    public String title() {
        return "Choerodon通知";
    }

    @Override
    public String content() {
        return "<p>项目“${projectName}”中的流水线“${pipelineName}”执行成功</p>";
    }
}
