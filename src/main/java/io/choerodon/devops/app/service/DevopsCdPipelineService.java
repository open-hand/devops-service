package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.PipelineWebHookVO;

public interface DevopsCdPipelineService {

    /**
     * 处理ci流水线状态变更
     * ci流水线状态为pendding、running,计算cd要执行的阶段、任务，并更新cd流水线状态为未开始
     * ci流水线状态为success， 执行cd流水线
     *
     * @param pipelineWebHookVO ci流水线记录信息
     */
    void handleCiPipelineStatusUpdate(PipelineWebHookVO pipelineWebHookVO);

    void triggerCdPipeline(String token, String commit);
}
