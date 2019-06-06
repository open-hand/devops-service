package io.choerodon.devops.infra.config;

import io.choerodon.core.notify.PmTemplate;
import io.choerodon.devops.infra.common.util.enums.PipelineNoticeType;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  11:34 2019/6/6
 * Description:
 */
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
        return PipelineNoticeType.PIPELINESUCCESS.toValue();
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
