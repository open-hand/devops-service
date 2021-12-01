package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.PipelineTemplateVO;
import io.choerodon.devops.api.vo.pipeline.PipelineTemplateCompositeVO;
import io.choerodon.devops.app.service.CiTemplateStageJobRelService;
import io.choerodon.devops.app.service.CiTemplateStageService;
import io.choerodon.devops.app.service.PipelineTemplateService;
import io.choerodon.devops.infra.dto.PipelineTemplateDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.PipelineTemplateMapper;

/**
 * 流水线模板表(PipelineTemplate)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 16:54:19
 */
@Service
public class PipelineTemplateServiceImpl implements PipelineTemplateService {

    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;

    @Autowired
    private CiTemplateStageService ciTemplateStageService;

    @Autowired
    private CiTemplateStageJobRelService ciTemplateStageJobRelService;

    @Autowired
    private PipelineTemplateMapper pipelineTemplatemapper;


    @Override
    public PipelineTemplateCompositeVO listTemplateWithLanguage(Long projectId) {
        List<PipelineTemplateDTO> pipelineTemplateDTOS = listTemplateForProject(projectId);

        return null;
    }

    @Override
    public List<PipelineTemplateDTO> listTemplateForProject(Long projectId) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
        List<PipelineTemplateVO> pipelineTemplateVOS = pipelineTemplatemapper.listTemplateForProject(projectDTO.getOrganizationId());

        return;
    }


}

