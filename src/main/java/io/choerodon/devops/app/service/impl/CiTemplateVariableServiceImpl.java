package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.CiTemplateVariableService;
import io.choerodon.devops.infra.mapper.CiTemplateVariableMapper;

/**
 * 流水线模板配置的CI变量(CiTemplateVariable)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:22
 */
@Service
public class CiTemplateVariableServiceImpl implements CiTemplateVariableService {
    @Autowired
    private CiTemplateVariableMapper ciTemplateVariablemapper;


}

