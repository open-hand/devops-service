package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.PipelinePersonalTokenDTO;

/**
 * 流水线个人token表(PipelinePersonalToken)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-12-05 11:20:48
 */
public interface PipelinePersonalTokenService {

    String queryOrCreatePersonalToken(Long projectId);

    String resetPersonalToken(Long projectId);

    PipelinePersonalTokenDTO queryByToken(String token);

    PipelinePersonalTokenDTO queryByTokenOrThrowE(String token);
}

