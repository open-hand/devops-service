package io.choerodon.devops.infra.template;

import org.springframework.stereotype.Component;

import io.choerodon.core.notify.*;
import io.choerodon.devops.infra.enums.PipelineNoticeType;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:50 2019/6/6
 * Description:
 */
@NotifyBusinessType(code = "pipelinefailed", name = "流水线执行失败", level = Level.PROJECT,
        description = "流水线执行失败通知", isManualRetry = true, categoryCode = "stream-change-notice",
        pmEnabledFlag = true,
        emailEnabledFlag = true,
        proPmEnabledFlag = true,
        notifyType = ServiceNotifyType.DEVOPS_NOTIFY, targetUserType = {TargetUserType.TARGET_USER_PIPELINE_TRIGGERSS})
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
                "<p><a href=#/devops/deployment-operation?type=project&id=${projectId}&name=${projectName}&category=undefined&organizationId=${organizationId}&orgId=${organizationId}&pipelineRecordId=${pipelineRecordId}>查看详情</a >";
    }
}
