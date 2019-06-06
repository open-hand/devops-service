package io.choerodon.devops.infra.config;

import io.choerodon.core.notify.PmTemplate;
import io.choerodon.devops.infra.common.util.enums.PipelineNoticeType;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:35 2019/6/6
 * Description:
 */
public class PipelineAuditPmTemplate implements PmTemplate {
    @Override
    public String businessTypeCode() {
        return "pipeline";
    }

    @Override
    public String code() {
        return PipelineNoticeType.PIPELINEAUDIT.toValue();
    }

    @Override
    public String name() {
        return PipelineNoticeType.PIPELINEAUDIT.toValue();
    }

    @Override
    public String title() {
        return "流水线任务待审核";
    }

    @Override
    public String content() {
        return "<p>流水线“${pipelineName}”目前暂停于【${stageName}】<p>" +
                "<p><a href=#/devops/pipeline-record/detail/${pipelineId}/${pipelineRecordId}?type=project&id=${projectId}&name=${projectName}&category=undefined&organizationId=${organizationId}>查看详情</a >";
    }
}
