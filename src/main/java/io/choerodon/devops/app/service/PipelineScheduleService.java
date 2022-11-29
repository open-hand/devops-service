package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.cd.PipelineScheduleVO;

/**
 * 流水线定时配置表(PipelineSchedule)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-11-29 16:05:20
 */
public interface PipelineScheduleService {

    void create(Long pipelineId, PipelineScheduleVO pipelineScheduleVO);
}

