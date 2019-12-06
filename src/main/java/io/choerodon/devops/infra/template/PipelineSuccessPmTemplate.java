package io.choerodon.devops.infra.template;

import org.springframework.stereotype.Component;

import io.choerodon.core.notify.*;
import io.choerodon.devops.infra.enums.PipelineNoticeType;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  11:34 2019/6/6
 * Description:
 */
@NotifyBusinessType(code = "pipelinesuccess", name = "流水线执行成功", level = Level.PROJECT,
        description = "流水线执行成功通知", isManualRetry = true, categoryCode = "stream-change-notice",
        pmEnabledFlag = true,
        emailEnabledFlag = true,
        proPmEnabledFlag = true,
        notifyType = ServiceNotifyType.DEVOPS_NOTIFY, targetUserType = {TargetUserType.TARGET_USER_PIPELINE_TRIGGERSS})
@Component
public class PipelineSuccessPmTemplate implements PmTemplate {
    @Override
    public String businessTypeCode() {
        return PipelineNoticeType.PIPELINESUCCESS.toValue();
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
        return "<p>流水线“${pipelineName}”执行成功</p>" +
                "<p><a href=#/devops/deployment-operation?type=project&id=${projectId}&name=${projectName}&category=undefined&organizationId=${organizationId}&orgId=${organizationId}&pipelineRecordId=${pipelineRecordId}>查看详情</a >;\n";
    }
}
