package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.CiTemplateMavenBuildDTO;

/**
 * devops_ci_template_maven_build(CiTemplateMavenBuild)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-15 14:06:07
 */
public interface CiTemplateMavenBuildService {

    CiTemplateMavenBuildDTO baseQueryById(Long stepId);

    void baseCreate(Long id, CiTemplateMavenBuildDTO mavenBuildConfig);

    CiTemplateMavenBuildDTO dtoToVo(CiTemplateMavenBuildDTO ciTemplateMavenBuildDTO);

    CiTemplateMavenBuildDTO voToDto(CiTemplateMavenBuildDTO ciTemplateMavenBuildDTO);

    void deleteByTemplateStepId(Long templateStepId);

}

