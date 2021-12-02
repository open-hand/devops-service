package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.PipelineTemplateVO;
import io.choerodon.devops.api.vo.pipeline.PipelineTemplateCompositeVO;
import io.choerodon.devops.app.service.CiTemplateLanguageService;
import io.choerodon.devops.app.service.CiTemplateStageJobRelService;
import io.choerodon.devops.app.service.CiTemplateStageService;
import io.choerodon.devops.app.service.PipelineTemplateService;
import io.choerodon.devops.infra.dto.CiTemplateLanguageDTO;
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
    private CiTemplateLanguageService ciTemplateLanguageService;

    @Autowired
    private PipelineTemplateMapper pipelineTemplatemapper;


    @Override
    public PipelineTemplateCompositeVO listTemplateWithLanguage(Long projectId) {
        List<PipelineTemplateVO> pipelineTemplateVOS = listTemplateForProject(projectId);

        Set<Long> languageIds = pipelineTemplateVOS.stream().map(PipelineTemplateVO::getCiTemplateLanguageId).collect(Collectors.toSet());
        List<CiTemplateLanguageDTO> ciTemplateLanguageDTOS = ciTemplateLanguageService.listByIds(languageIds);

        return new PipelineTemplateCompositeVO(ciTemplateLanguageDTOS, pipelineTemplateVOS);
    }

    @Override
    public List<PipelineTemplateVO> listTemplateForProject(Long projectId) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
        return pipelineTemplatemapper.listTemplateForProject(projectDTO.getOrganizationId());
    }


}

