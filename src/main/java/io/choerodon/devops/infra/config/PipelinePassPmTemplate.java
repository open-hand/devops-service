package io.choerodon.devops.infra.config;

import org.springframework.stereotype.Component;

import io.choerodon.core.notify.Level;
import io.choerodon.core.notify.NotifyBusinessType;
import io.choerodon.core.notify.PmTemplate;
import io.choerodon.devops.infra.common.util.enums.PipelineNoticeType;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:41 2019/6/6
 * Description:
 */
@NotifyBusinessType(code = "pipelinepass", name = "流水线或签任务通过通知", level = Level.SITE,
        description = "流水线或签任务通过通知", isAllowConfig = false, isManualRetry = true)
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
        return "<p>流水线“${pipelineName}”在【${stageName}】中的或签任务已被${auditName}:${realName}审核<p>" +
                "<p><a href=#/devops/pipeline-record/detail/${pipelineId}/${pipelineRecordId}?type=project&id=${projectId}&name=${projectName}&category=undefined&organizationId=${organizationId}>查看详情</a >";
    }
}