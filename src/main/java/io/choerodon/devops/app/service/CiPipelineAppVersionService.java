package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.CiPipelineAppVersionDTO;

/**
 * 发布应用服务版本步骤生成的流水线记录信息(CiPipelineAppVersion)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-07-14 16:01:30
 */
public interface CiPipelineAppVersionService {

    CiPipelineAppVersionDTO queryByPipelineIdAndJobName(Long appServiceId,
                                                        Long gitlabPipelineId,
                                                        String jobName);

    void baseCreate(CiPipelineAppVersionDTO ciPipelineAppVersionDTO);

    void deleteByAppServiceId(Long appServiceId);
}

