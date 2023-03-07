package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.pipeline.CiAuditConfigVO;
import io.choerodon.devops.infra.dto.CiAuditConfigDTO;

/**
 * ci 人工卡点配置表(CiAuditConfig)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-11-02 11:40:27
 */
public interface CiAuditConfigService {

    CiAuditConfigVO queryConfigWithUsersById(Long id);

    CiAuditConfigVO queryConfigWithUserDetailsById(Long id);

    CiAuditConfigDTO baseCreate(CiAuditConfigDTO ciAuditConfigDTO);

    List<CiAuditConfigDTO> listByCiPipelineId(Long ciPipelineId);

    void batchDeleteByIds(List<Long> ids);

    void deleteConfigByPipelineId(Long ciPipelineId);
}

