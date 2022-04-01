package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.CiDockerAuthConfigDTO;

/**
 * 流水线配置的docker认证配置(CiDockerAuthConfig)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2022-03-15 09:54:20
 */
public interface CiDockerAuthConfigService {

    void baseCreate(CiDockerAuthConfigDTO ciDockerAuthConfigDTO);

    void deleteByPipelineId(Long pipelineId);

    List<CiDockerAuthConfigDTO> listByPipelineId(Long pipelineId);
}

