package io.choerodon.devops.app.service;

public interface DevopsCheckLogService {

    /**
     * 平滑升级
     *
     * @param version 版本
     */
    void checkLog(String version);


    void migrationCdPipelineDate();

    void fixPipeline(Long pipelineId);

    void fixCiTemplateStageJobRelSequence();
}
