package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.DevopsPipelineRecordRelDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/14 20:52
 */
public interface DevopsPipelineRecordRelService {

    void save(DevopsPipelineRecordRelDTO devopsPipelineRecordRelDTO);

    DevopsPipelineRecordRelDTO queryByPipelineIdAndCiPipelineRecordId(Long pipelineId, Long ciPipelineRecordId);

    void update(DevopsPipelineRecordRelDTO devopsPipelineRecordRelDTO);
}
