package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.DevopsCdPipelineRecordDTO;
import io.choerodon.devops.infra.dto.workflow.DevopsPipelineDTO;

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

    void updateStatusById(Long pipelineRecordId, String status);

    DevopsPipelineDTO createCDWorkFlowDTO(Long pipelineRecordId);

    /**
     * 主机模式 镜像部署
     */
    void cdHostImageDeploy(Long pipelineRecordId, Long cdStageRecordId, Long cdJobRecordId);


    void update(DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO);

    void deleteByPipelineId(Long pipelineId);

    DevopsCdPipelineRecordDTO queryById(Long id);


}
