package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.CiTemplateSonarService;
import io.choerodon.devops.infra.mapper.CiTemplateSonarMapper;

/**
 * devops_ci_template_sonar(CiTemplateSonar)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:19
 */
@Service
public class CiTemplateSonarServiceImpl implements CiTemplateSonarService {
    @Autowired
    private CiTemplateSonarMapper ciTemplateSonarmapper;


}

