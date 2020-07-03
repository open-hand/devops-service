package io.choerodon.devops.app.service;

/**
 * @author scp
 * @date 2020/7/3
 * @description cd 流水线方法 暂时放这个service
 */
public interface CdPipelineService {

    /**
     * 主机模式 镜像部署
     */
    void cdHostImageDeploy(Long pipelineRecordId, Long cdStageRecordId, Long cdJobRecordId);
}
