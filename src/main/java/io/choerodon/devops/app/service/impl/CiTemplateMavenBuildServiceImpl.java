package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.CiTemplateMavenBuildService;
import io.choerodon.devops.infra.dto.CiTemplateMavenBuildDTO;
import io.choerodon.devops.infra.mapper.CiTemplateMavenBuildMapper;

/**
 * devops_ci_template_maven_build(CiTemplateMavenBuild)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-15 14:06:08
 */
@Service
public class CiTemplateMavenBuildServiceImpl implements CiTemplateMavenBuildService {
    @Autowired
    private CiTemplateMavenBuildMapper ciTemplateMavenBuildMapper;


    @Override
    public CiTemplateMavenBuildDTO baseQueryById(Long stepId) {
        CiTemplateMavenBuildDTO ciTemplateMavenBuildDTO = new CiTemplateMavenBuildDTO();
        ciTemplateMavenBuildDTO.setCiTemplateStepId(stepId);
        return ciTemplateMavenBuildMapper.selectOne(ciTemplateMavenBuildDTO);
    }
}

