package io.choerodon.devops.infra.config;

import org.springframework.stereotype.Component;

import io.choerodon.core.notify.Level;
import io.choerodon.core.notify.NotifyBusinessType;
import io.choerodon.core.notify.PmTemplate;
import io.choerodon.devops.infra.common.util.enums.PipelineNoticeType;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  11:34 2019/6/6
 * Description:
 */
@NotifyBusinessType(code = "pipelinesuccess", name = "流水线成功通知", level = Level.SITE,
        description = "流水线成功通知", isAllowConfig = false, isManualRetry = true)
@Component
public class PipelineSuccessPmTemplate implements PmTemplate {
    @Override
    public String businessTypeCode() {
        return "pipeline";
    }

    @Override
    public String code() {
        return PipelineNoticeType.PIPELINESUCCESS.toValue();
    }

    @Override
    public String name() {
        return "流水线成功通知";
    }

    @Override
    public String title() {
        return "流水线成功";
    }

    @Override
    public String content() {
        return "<p>流水线“${pipelineName}”执行成功</p>"+
                "<p><a href=#/devops/pipeline-record/detail/${pipelineId}/${pipelineRecordId}?type=project&id=${projectId}&name=${projectName}&category=${category}&organizationId=${organizationId}>查看详情</a >\";\n";
    }
}
