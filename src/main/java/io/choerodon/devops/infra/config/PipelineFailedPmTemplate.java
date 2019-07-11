package io.choerodon.devops.infra.config;

import org.springframework.stereotype.Component;

import io.choerodon.core.notify.Level;
import io.choerodon.core.notify.NotifyBusinessType;
import io.choerodon.core.notify.PmTemplate;
import io.choerodon.devops.infra.enums.PipelineNoticeType;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:50 2019/6/6
 * Description:
 */
@NotifyBusinessType(code = "pipelinefailed", name = "流水线失败通知", level = Level.SITE,
        description = "流水线失败通知", isAllowConfig = false, isManualRetry = true)
@Component
public class PipelineFailedPmTemplate implements PmTemplate {
    @Override
    public String businessTypeCode() {
        return PipelineNoticeType.PIPELINEFAILED.toValue();
    }

    @Override
    public String code() {
        return PipelineNoticeType.PIPELINEFAILED.toValue();
    }

    @Override
    public String name() {
        return "流水线失败通知";
    }

    @Override
    public String title() {
        return "流水线失败";
    }

    @Override
    public String content() {
        return "<p>流水线“${pipelineName}”执行失败<p>" +
                "<p><a href=#/devops/pipeline-record/detail/${pipelineId}/${pipelineRecordId}?type=project&id=${projectId}&name=${projectName}&category=undefined&organizationId=${organizationId}>查看详情</a >";
    }
}
