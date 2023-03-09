package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.CiCdPipelineDTO;
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
     *
     * @param token 流水线token
     * @return ci内容
     */
    String queryLatestContent(String token);

    String queryLatestContent(CiCdPipelineDTO devopsCiPipelineDTO);

    /**
     * 保存流水线配置
     *
     * @param devopsCiContentDTO
     */
    void create(DevopsCiContentDTO devopsCiContentDTO);

    /**
     * 删除流水线ci文件
     *
     * @param ciPipelineId
     */
    void deleteByPipelineId(Long ciPipelineId);
}
