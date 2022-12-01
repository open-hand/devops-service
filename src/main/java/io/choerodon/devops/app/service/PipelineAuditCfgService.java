package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.cd.PipelineAuditCfgVO;
import io.choerodon.devops.infra.dto.PipelineAuditCfgDTO;

/**
 * 人工卡点配置表(PipelineAuditConfig)应用服务
 *
 * @author
 * @since 2022-11-24 15:56:37
 */
public interface PipelineAuditCfgService {


    void deleteConfigByPipelineId(Long pipelineId);

    void baseCreate(PipelineAuditCfgDTO pipelineAuditCfgDTO);

    List<PipelineAuditCfgDTO> listByPipelineId(Long pipelineId);

    PipelineAuditCfgVO queryConfigWithUsersById(Long id);

    PipelineAuditCfgVO queryConfigWithUserDetailsById(Long id);
}

