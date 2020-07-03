package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.DevopsCdPipelineRecordDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/2 10:41
 */
public interface DevopsCdPipelineRecordService {

    DevopsCdPipelineRecordDTO queryByGitlabPipelineId(Long gitlabPipelineId);

    void save(DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO);

    /**
     * 主机模式 镜像部署
     */
    void cdHostImageDeploy(Long pipelineRecordId, Long cdStageRecordId, Long cdJobRecordId);
}
