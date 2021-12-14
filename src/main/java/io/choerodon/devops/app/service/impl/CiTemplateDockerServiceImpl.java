package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.CiTemplateDockerService;
import io.choerodon.devops.infra.mapper.CiTemplateDockerMapper;

/**
 * 流水线任务模板与步骤模板关系表(CiTemplateDocker)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:56:48
 */
@Service
public class CiTemplateDockerServiceImpl implements CiTemplateDockerService {
    @Autowired
    private CiTemplateDockerMapper ciTemplateDockermapper;

}

