package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.CiTemplateMavenPublishService;
import io.choerodon.devops.infra.dto.CiTemplateMavenPublishDTO;
import io.choerodon.devops.infra.mapper.CiTemplateMavenPublishMapper;

/**
 * devops_ci_template_maven_publish(CiTemplateMavenPublish)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-15 14:06:09
 */
@Service
public class CiTemplateMavenPublishServiceImpl implements CiTemplateMavenPublishService {
    @Autowired
    private CiTemplateMavenPublishMapper ciTemplateMavenPublishMapper;


    @Override
    public CiTemplateMavenPublishDTO queryByStepId(Long stepId) {
        CiTemplateMavenPublishDTO ciTemplateMavenPublishDTO = new CiTemplateMavenPublishDTO();
        ciTemplateMavenPublishDTO.setCiTemplateStepId(stepId);
        return ciTemplateMavenPublishMapper.selectOne(ciTemplateMavenPublishDTO);
    }
}

