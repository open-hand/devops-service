package io.choerodon.devops.domain.application.repository;

import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.iam.entity.PipelineRecordE;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:25 2019/4/4
 * Description:
 */
public interface PipelineRecordRepository {
    PageInfo<PipelineRecordE> basePageByOptions(Long projectId, Long pipelineId, PageRequest pageRequest, String params, Map<String, Object> classifyParam);

    PipelineRecordE baseCreate(PipelineRecordE pipelineRecordE);

    PipelineRecordE baseUpdate(PipelineRecordE pipelineRecordE);

    PipelineRecordE baseQueryById(Long recordId);

    List<PipelineRecordE> baseQueryByPipelineId(Long pipelineId);

    void baseUpdateWithEdited(Long pipelineId);

    List<Long> baseQueryAllRecordUserIds(Long pipelineRecordId);

}
