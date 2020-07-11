package io.choerodon.devops.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.DevopsCdPipelineRecordVO;
import io.choerodon.devops.api.vo.HostConnectionVO;
import io.choerodon.devops.infra.dto.DevopsCdPipelineRecordDTO;
import io.choerodon.devops.infra.dto.workflow.DevopsPipelineDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

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

    DevopsPipelineDTO createCDWorkFlowDTO(Long pipelineRecordId, Boolean isRetry);

    /**
     * 主机模式 镜像部署
     */
    Boolean cdHostImageDeploy(Long pipelineRecordId, Long cdStageRecordId, Long cdJobRecordId);

    Boolean cdHostJarDeploy(Long pipelineRecordId, Long cdStageRecordId, Long cdJobRecordId);

    void cdHostDeploy(Long pipelineRecordId, Long cdStageRecordId, Long cdJobRecordId);

    void retryHostDeployJob(Long pipelineRecordId, Long cdStageRecordId, Long cdJobRecordId);

    void update(DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO);

    void deleteByPipelineId(Long pipelineId);

    DevopsCdPipelineRecordDTO queryById(Long id);

    Page<DevopsCdPipelineRecordVO> pagingCdPipelineRecord(Long projectId, Long ciPipelineId, PageRequest pageable);


    void updatePipelineStatusFailed(Long pipelineRecordId, String errorInfo);

    DevopsCdPipelineRecordVO queryPipelineRecordDetails(Long projectId, Long gitlabPipelineId);


    Boolean testConnection(HostConnectionVO hostConnectionVO);
}
