package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.CiTemplateMavenPublishDTO;

/**
 * devops_ci_template_maven_publish(CiTemplateMavenPublish)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-15 14:06:09
 */
public interface CiTemplateMavenPublishService {

    CiTemplateMavenPublishDTO queryByStepId(Long stepId);

    void baseCreate(Long id, CiTemplateMavenPublishDTO mavenBuildConfig);

    CiTemplateMavenPublishDTO dtoToVo(CiTemplateMavenPublishDTO ciTemplateMavenPublishDTO);

    CiTemplateMavenPublishDTO voToDto(CiTemplateMavenPublishDTO ciTemplateMavenPublishDTO) ;

    void deleteByTemplateId(Long templateStepId);
}

