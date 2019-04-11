package io.choerodon.devops.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.PipelineDTO;
import io.choerodon.devops.api.dto.PipelineRecordDTO;
import io.choerodon.devops.api.dto.PipelineReqDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

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

    void setTaskStatus(Long taskRecordId, String proInstanceId);

    /**
     * 人工审核
     *
     * @param recordId
     * @param type
     * @param isApprove
     */
    void audit(Long projectId, Long pipelineRecordId, Long recordId, String type, Boolean isApprove);
}
