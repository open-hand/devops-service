package io.choerodon.devops.infra.config;

import org.springframework.stereotype.Component;

import io.choerodon.core.notify.PmTemplate;
import io.choerodon.devops.infra.common.util.enums.PipelineNoticeType;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:38 2019/6/6
 * Description:
 */
@Component
public class PipelineStopPmTemplate implements PmTemplate {
    @Override
    public String businessTypeCode() {
        return "pipeline";
    }

    @Override
    public String code() {
        return PipelineNoticeType.PIPELINESTOP.toValue();
    }

    @Override
    public String name() {
        return "流水线被终止通知";
    }

    @Override
    public String title() {
        return "流水线被终止";
    }

    @Override
    public String content() {
        return "<p>流水线“${pipelineName}”在【${stageName}】被${loginName}:${realName}终止<p>" +
                "<p><a href=#/devops/pipeline-record/detail/${pipelineId}/${pipelineRecordId}?type=project&id=${projectId}&name=${projectName}&category=undefined&organizationId=${organizationId}>查看详情</a >";
    }
}
