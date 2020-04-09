package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.DevopsCiContentDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:25
 */
public interface DevopsCiContentService {
    /**
     * 查询流水线的最新配置
     * @param pipelineId
     * @return
     */
    String queryLatestContent(Long pipelineId);

    /**
     * 保存流水线配置
     * @param devopsCiContentDTO
     */
    void create(DevopsCiContentDTO devopsCiContentDTO);

    void deleteByPipelineId(Long ciPipelineId);
}
