package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageInfo;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.infra.dto.PipelineRecordDTO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  15:09 2019/7/15
 * Description:
 */
public interface PipelineRecordService {

    PageInfo<PipelineRecordDTO> basePageByOptions(Long projectId, Long pipelineId, PageRequest pageRequest, String params, Map<String, Object> classifyParam);

    PipelineRecordDTO baseCreate(PipelineRecordDTO pipelineRecordDTO);

    PipelineRecordDTO baseUpdate(PipelineRecordDTO pipelineRecordDTO);

    PipelineRecordDTO baseQueryById(Long recordId);

    List<PipelineRecordDTO> baseQueryByPipelineId(Long pipelineId);

    void baseUpdateWithEdited(Long pipelineId);

    List<Long> baseQueryAllRecordUserIds(Long pipelineRecordId);
}
