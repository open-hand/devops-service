package io.choerodon.devops.domain.application.repository;

import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.iam.entity.PipelineE;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:19 2019/4/4
 * Description:
 */
public interface PipelineRepository {
    PageInfo<PipelineE> baseListByOptions(Long projectId, PageRequest pageRequest, String params, Map<String, Object> classifyParam);

    PipelineE baseCreate(Long projectId, PipelineE pipelineE);

    PipelineE baseUpdate(Long projectId, PipelineE pipelineE);

    PipelineE baseUpdateWithEnabled(Long pipelineId, Integer isEnabled);

    PipelineE baseQueryById(Long pipelineId);

    void baseDelete(Long pipelineId);

    void baseCheckName(Long projectId, String name);

    List<PipelineE> baseQueryByProjectId(Long projectId);
}
