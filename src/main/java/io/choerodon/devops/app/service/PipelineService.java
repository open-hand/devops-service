package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageInfo;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.notify.NoticeSendDTO;
import io.choerodon.devops.api.dto.CheckAuditDTO;
import io.choerodon.devops.api.dto.IamUserDTO;
import io.choerodon.devops.api.dto.PipelineCheckDeployDTO;
import io.choerodon.devops.api.dto.PipelineDTO;
import io.choerodon.devops.api.dto.PipelineRecordDTO;
import io.choerodon.devops.api.dto.PipelineRecordListDTO;
import io.choerodon.devops.api.dto.PipelineRecordReqDTO;
import io.choerodon.devops.api.dto.PipelineReqDTO;
import io.choerodon.devops.api.dto.PipelineUserRecordRelDTO;
import io.choerodon.devops.api.dto.iam.UserDTO;
import io.choerodon.devops.infra.dataobject.workflow.DevopsPipelineDTO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:57 2019/4/3
 * Description:
 */
public interface PipelineService {
    PageInfo<PipelineDTO> listByOptions(Long projectId, Boolean creator, Boolean executor, List<String> envIds, PageRequest pageRequest, String params);

    PageInfo<PipelineRecordDTO> listRecords(Long projectId, Long pipelineId, PageRequest pageRequest, String params, Boolean pendingcheck, Boolean executed, Boolean reviewed);

    PipelineReqDTO create(Long projectId, PipelineReqDTO pipelineReqDTO);

    PipelineReqDTO update(Long projectId, PipelineReqDTO pipelineReqDTO);

    PipelineDTO updateIsEnabled(Long projectId, Long pipelineId, Integer isEnabled);

    void delete(Long projectId, Long pipelineId);

    PipelineReqDTO queryById(Long projectId, Long pipelineId);

    void execute(Long projectId, Long pipelineId);

    void autoDeploy(Long stageRecordId, Long taskId);

    List<IamUserDTO> audit(Long projectId, PipelineUserRecordRelDTO userRecordRelDTO);

    PipelineCheckDeployDTO checkDeploy(Long projectId, Long pipelineId);

    DevopsPipelineDTO createWorkFlowDTO(Long pipelineRecordId, Long pipelineId, String businessKey);

    String getAppDeployStatus(Long stageRecordId, Long taskId);

    void setAppDeployStatus(Long pipelineRecordId, Long stageRecordId, Long taskId, Boolean status);

    PipelineRecordReqDTO getRecordById(Long projectId, Long pipelineRecordId);

    void retry(Long projectId, Long pipelineRecordId);

    List<PipelineRecordListDTO> queryByPipelineId(Long pipelineId);

    void checkName(Long projectId, String name);

    List<PipelineDTO> listPipelineDTO(Long projectId);

    List<UserDTO> getAllUsers(Long projectId);

    void updateStatus(Long pipelineRecordId, Long stageRecordId, String status, String errorInfo);

    CheckAuditDTO checkAudit(Long projectId, PipelineUserRecordRelDTO userRecordRelDTO);

    void executeAutoDeploy(Long pipelineId);

    void failed(Long projectId, Long recordId);

    void sendSiteMessage(Long pipelineRecordId, String type, List<NoticeSendDTO.User> users, Map<String, Object> params);
}
