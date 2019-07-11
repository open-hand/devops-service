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
    PageInfo<PipelineRecordE> listByOptions(Long projectId, Long pipelineId, PageRequest pageRequest, String params, Map<String, Object> classifyParam);

    PipelineRecordE create(PipelineRecordE pipelineRecordE);

    PipelineRecordE update(PipelineRecordE pipelineRecordE);

    PipelineRecordE queryById(Long recordId);

    List<PipelineRecordE> queryByPipelineId(Long pipelineId);

    void updateEdited(Long pipelineId);

    List<Long> queryAllRecordUserIds(Long pipelineRecordId);

}
