package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.infra.dto.PipelineDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:57 2019/4/3
 * Description:
 */
public interface PipelineService {
    Page<PipelineVO> pageByOptions(Long projectId, PipelineSearchVO pipelineSearchVO, PageRequest pageable);

    PipelineReqVO create(Long projectId, PipelineReqVO pipelineReqVO);

    PipelineReqVO update(Long projectId, PipelineReqVO pipelineReqVO);

    PipelineVO updateIsEnabled(Long projectId, Long pipelineId, Integer isEnabled);

    void delete(Long projectId, Long pipelineId);

    PipelineReqVO queryById(Long projectId, Long pipelineId);

    void execute(Long projectId, Long pipelineId);

    void batchExecute(Long projectId, Long[] pipelineIds);

    void autoDeploy(Long stageRecordId, Long taskId);

    List<PipelineUserVO> audit(Long projectId, PipelineUserRecordRelationshipVO userRecordRelDTO);

    PipelineCheckDeployVO checkDeploy(Long projectId, Long pipelineId);

    io.choerodon.devops.infra.dto.workflow.DevopsPipelineDTO createWorkFlowDTO(Long pipelineRecordId, Long pipelineId, String businessKey);

    String getAppDeployStatus(Long stageRecordId, Long taskId);

    void setAppDeployStatus(Long pipelineRecordId, Long stageRecordId, Long taskId, Boolean status);

    PipelineRecordReqVO getRecordById(Long projectId, Long pipelineRecordId);

    void retry(Long projectId, Long pipelineRecordId);

    List<PipelineRecordListVO> queryByPipelineId(Long pipelineId);

    void checkName(Long projectId, String name);

    boolean isNameUnique(Long projectId, String name);

    List<PipelineVO> listPipelineDTO(Long projectId);

    void updateStatus(Long pipelineRecordId, Long stageRecordId, String status, String errorInfo);

    CheckAuditVO checkAudit(Long projectId, PipelineUserRecordRelationshipVO userRecordRelDTO);

    void failed(Long projectId, Long recordId);

    PipelineDTO baseCreate(Long projectId, PipelineDTO devopsPipelineDTO);

    PipelineDTO baseUpdate(Long projectId, PipelineDTO devopsPipelineDTO);

    PipelineDTO baseUpdateWithEnabled(Long pipelineId, Integer isEnabled);

    PipelineDTO baseQueryById(Long pipelineId);

    void baseDelete(Long pipelineId);

    void baseCheckName(Long projectId, String name);

    List<PipelineDTO> baseQueryByProjectId(Long projectId);

    void setPipelineRecordDetail(Boolean projectOwner, DevopsDeployRecordVO devopsDeployRecordVO);

    PipelineDTO checkExistAndGet(Long pipelineId);

    void executeAutoDeploy(Long pipelineId);
}
