package io.choerodon.devops.infra.template;

import org.springframework.stereotype.Component;

import io.choerodon.core.notify.*;
import io.choerodon.devops.infra.enums.PipelineNoticeType;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:41 2019/6/6
 * Description:
 */
@NotifyBusinessType(code = "pipelinepass", name = "流水线或签任务通过通知", level = Level.PROJECT,
        pmEnabledFlag = true,
        emailEnabledFlag = true,
        proPmEnabledFlag = true,
        description = "流水线或签任务通过通知", isAllowConfig = false, isManualRetry = true, categoryCode = "stream-change-notice")
@Component
public class PipelinePassPmTemplate implements PmTemplate {
    @Override
    public String businessTypeCode() {
        return PipelineNoticeType.PIPELINEPASS.toValue();
    }

    @Override
    public String code() {
        return PipelineNoticeType.PIPELINEPASS.toValue();
    }

    @Override
    public String name() {
        return "流水线或签任务通过通知";
    }

    @Override
    public String title() {
        return "或签任务已通过";
    }

    @Override
    public String content() {
        return "<p>流水线“${pipelineName}”在【${stageName}】阶段中的或签任务已被${auditName}:${realName}审核<p>" +
                "<p><a href=#/devops/deployment-operation?type=project&id=${projectId}&name=${projectName}&category=undefined&organizationId=${organizationId}&orgId=${organizationId}&pipelineRecordId=${pipelineRecordId}>查看详情</a >";
    }
}