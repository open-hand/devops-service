package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.template.CiTemplateStepVO;
import io.choerodon.devops.app.service.AbstractDevopsCiStepHandler;
import io.choerodon.devops.app.service.CiTemplateStepService;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.CiTemplateDockerMapper;
import io.choerodon.devops.infra.mapper.CiTemplateSonarMapper;
import io.choerodon.devops.infra.mapper.CiTemplateStepMapper;

/**
 * 流水线步骤模板(CiTemplateStep)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:21
 */
@Service
public class CiTemplateStepServiceImpl implements CiTemplateStepService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CiTemplateStepServiceImpl.class);

    @Autowired
    private CiTemplateStepMapper ciTemplateStepMapper;

    @Autowired
    private CiTemplateDockerMapper ciTemplateDockerMapper;

    @Autowired
    private CiTemplateSonarMapper ciTemplateSonarMapper;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private DevopsCiStepOperator devopsCiStepOperator;


    @Override
    public List<CiTemplateStepVO> listByJobIds(Set<Long> jobIds) {
        return ciTemplateStepMapper.listByJobIds(jobIds);
    }

    @Override
    public List<CiTemplateStepVO> listStepsByTemplateJobId(Long projectId, Long templateJobId) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
        List<CiTemplateStepVO> ciTemplateStepVOS = ciTemplateStepMapper.listStepsByTemplateJobId(projectDTO.getOrganizationId(), templateJobId);
        ciTemplateStepVOS.forEach(ciTemplateStepVO -> {
            AbstractDevopsCiStepHandler devopsCiStepHandler = devopsCiStepOperator.getHandlerOrThrowE(ciTemplateStepVO.getType());
            devopsCiStepHandler.fillTemplateStepConfigInfo(ciTemplateStepVO);
        });

        return ciTemplateStepVOS;
    }

    @Override
    public List<CiTemplateStepVO> listStepsByProjectId(Long projectId) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
        List<CiTemplateStepVO> ciTemplateStepVOS = ciTemplateStepMapper.listStepsByOrganizationIdId(projectDTO.getOrganizationId());
        ciTemplateStepVOS.forEach(ciTemplateStepVO -> {
            AbstractDevopsCiStepHandler devopsCiStepHandler = devopsCiStepOperator.getHandlerOrThrowE(ciTemplateStepVO.getType());
            devopsCiStepHandler.fillTemplateStepConfigInfo(ciTemplateStepVO);
        });

        return ciTemplateStepVOS;
    }
}

