package io.choerodon.devops.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.PipelineDTO;
import io.choerodon.devops.api.dto.PipelineRecordDTO;
import io.choerodon.devops.api.dto.PipelineRecordReqDTO;
import io.choerodon.devops.api.dto.PipelineReqDTO;
import io.choerodon.devops.api.dto.PipelineUserRecordRelDTO;
import io.choerodon.devops.infra.dataobject.workflow.DevopsPipelineDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:57 2019/4/3
 * Description:
 */
public interface PipelineService {
    Page<PipelineDTO> listByOptions(Long projectId, PageRequest pageRequest, String params);

    Page<PipelineRecordDTO> listRecords(Long projectId, Long pipelineId, PageRequest pageRequest, String params);

    PipelineReqDTO create(Long projectId, PipelineReqDTO pipelineReqDTO);

    PipelineReqDTO update(Long projectId, PipelineReqDTO pipelineReqDTO);

    PipelineDTO updateIsEnabled(Long projectId, Long pipelineId, Integer isEnabled);

    void delete(Long projectId, Long pipelineId);

    PipelineReqDTO queryById(Long projectId, Long pipelineId);

    void execute(Long projectId, Long pipelineId);

    void autoDeploy(Long stageRecordId, Long taskId);

    void audit(Long projectId, PipelineUserRecordRelDTO userRecordRelDTO);

    Boolean checkDeploy(Long pipelineId);

    DevopsPipelineDTO setWorkFlowDTO(Long pipelineRecordId, Long pipelineId);

    String getAppDeployStatus(Long stageRecordId, Long taskId);

    void setAppDeployStatus(Long pipelineRecordId, Long stageRecordId, Long taskId);

    PipelineRecordReqDTO getRecordById(Long projectId, Long pipelineRecordId);

    void retry(Long projectId, Long pipelineRecordId);

    List<PipelineRecordDTO> queryByPipelineId(Long pipelineId);

    void checkName(Long projectId, String name);

    List<PipelineDTO> listPipelineDTO(Long projectId);
}
