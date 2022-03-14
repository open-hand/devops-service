package io.choerodon.devops.app.service;

public interface DevopsCheckLogService {

    /**
     * 平滑升级
     *
     * @param version 版本
     */
    void checkLog(String version);

    void devopsCiPipelineDataFix();

    void pipelineDataMavenPublishFix();

    /**
     * 流水线结构调整数据修复
     */
    void pipelineStructureFix();

    void pipelineSonarImageAndTemplateFix();

}
