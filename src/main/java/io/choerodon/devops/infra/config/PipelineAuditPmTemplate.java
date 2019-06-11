package io.choerodon.devops.infra.config;

import org.springframework.stereotype.Component;

import io.choerodon.core.notify.Level;
import io.choerodon.core.notify.NotifyBusinessType;
import io.choerodon.core.notify.PmTemplate;
import io.choerodon.devops.infra.common.util.enums.PipelineNoticeType;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:35 2019/6/6
 * Description:
 */
@NotifyBusinessType(code = "pipelineaudit", name = "流水线审核通知", level = Level.SITE,
        description = "流水线审核通知", isAllowConfig = false, isManualRetry = true)
@Component
public class PipelineAuditPmTemplate implements PmTemplate {
    @Override
    public String businessTypeCode() {
        return PipelineNoticeType.PIPELINEAUDIT.toValue();
    }

    @Override
    public String code() {
        return PipelineNoticeType.PIPELINEAUDIT.toValue();
    }

    @Override
    public String name() {
        return "流水线审核通知";
    }

    @Override
    public String title() {
        return "流水线任务待审核";
    }

    @Override
    public String content() {
        return "<p>流水线“${pipelineName}”目前暂停于【${stageName}】需要您进行审核<p>" +
                "<p><a href=#/devops/pipeline-record/detail/${pipelineId}/${pipelineRecordId}?type=project&id=${projectId}&name=${projectName}&category=undefined&organizationId=${organizationId}>查看详情</a >";
    }
}
