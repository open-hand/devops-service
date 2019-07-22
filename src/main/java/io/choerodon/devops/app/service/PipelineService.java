package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageInfo;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.notify.NoticeSendDTO;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.iam.UserVO;
import io.choerodon.devops.infra.dto.PipelineDTO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:57 2019/4/3
 * Description:
 */
public interface PipelineService {
    PageInfo<PipelineVO> pageByOptions(Long projectId, Boolean creator, Boolean executor, List<String> envIds, PageRequest pageRequest, String params);

    PageInfo<PipelineRecordVO> listRecords(Long projectId, Long pipelineId, PageRequest pageRequest, String params, Boolean pendingcheck, Boolean executed, Boolean reviewed);

    PipelineReqDTO create(Long projectId, PipelineReqDTO pipelineReqDTO);

    PipelineReqDTO update(Long projectId, PipelineReqDTO pipelineReqDTO);

    PipelineVO updateIsEnabled(Long projectId, Long pipelineId, Integer isEnabled);

    void delete(Long projectId, Long pipelineId);

    PipelineReqDTO queryById(Long projectId, Long pipelineId);

    void execute(Long projectId, Long pipelineId);

    void autoDeploy(Long stageRecordId, Long taskId);

    List<PipelineUserVO> audit(Long projectId, PipelineUserRecordRelationshipVO userRecordRelDTO);

    PipelineCheckDeployDTO checkDeploy(Long projectId, Long pipelineId);

    io.choerodon.devops.infra.dto.workflow.DevopsPipelineDTO createWorkFlowDTO(Long pipelineRecordId, Long pipelineId, String businessKey);

    String getAppDeployStatus(Long stageRecordId, Long taskId);

    void setAppDeployStatus(Long pipelineRecordId, Long stageRecordId, Long taskId, Boolean status);

    PipelineRecordReqDTO getRecordById(Long projectId, Long pipelineRecordId);

    void retry(Long projectId, Long pipelineRecordId);

    List<PipelineRecordListDTO> queryByPipelineId(Long pipelineId);

    void checkName(Long projectId, String name);

    List<PipelineVO> listPipelineDTO(Long projectId);

    List<UserVO> listAllUsers(Long projectId);

    void updateStatus(Long pipelineRecordId, Long stageRecordId, String status, String errorInfo);

    CheckAuditDTO checkAudit(Long projectId, PipelineUserRecordRelationshipVO userRecordRelDTO);

    void executeAutoDeploy(Long pipelineId);

    void failed(Long projectId, Long recordId);

    void sendSiteMessage(Long pipelineRecordId, String type, List<NoticeSendDTO.User> users, Map<String, Object> params);

    PageInfo<PipelineDTO> baseListByOptions(Long projectId, PageRequest pageRequest, String params, Map<String, Object> classifyParam);

    PipelineDTO baseCreate(Long projectId, PipelineDTO devopsPipelineDTO);

    PipelineDTO baseUpdate(Long projectId, PipelineDTO devopsPipelineDTO);

    PipelineDTO baseUpdateWithEnabled(Long pipelineId, Integer isEnabled);

    PipelineDTO baseQueryById(Long pipelineId);

    void baseDelete(Long pipelineId);

    void baseCheckName(Long projectId, String name);

    List<PipelineDTO> baseQueryByProjectId(Long projectId);
}
