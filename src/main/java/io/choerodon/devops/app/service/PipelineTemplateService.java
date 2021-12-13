package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.CiCdPipelineVO;
import io.choerodon.devops.api.vo.PipelineTemplateVO;
import io.choerodon.devops.api.vo.pipeline.PipelineTemplateCompositeVO;
import io.choerodon.devops.infra.dto.PipelineTemplateDTO;

/**
 * 流水线模板表(PipelineTemplate)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 16:54:19
 */
public interface PipelineTemplateService {

    PipelineTemplateCompositeVO listTemplateWithLanguage(Long projectId);

    PipelineTemplateDTO baseQuery(Long id);

    /**
     * 查询项目下可用的流水线模板
     *
     * @param projectId
     * @return
     */
    List<PipelineTemplateVO> listTemplateForProject(Long projectId);

    CiCdPipelineVO queryPipelineInfoByTemplateId(Long projectId, Long templateId);
}

