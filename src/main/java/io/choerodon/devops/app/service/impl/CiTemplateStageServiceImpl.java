package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.CiTemplateStageService;
import io.choerodon.devops.infra.mapper.CiTemplateStageMapper;

/**
 * 流水线模阶段(CiTemplateStage)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:20
 */
@Service
public class CiTemplateStageServiceImpl implements CiTemplateStageService {
    @Autowired
    private CiTemplateStageMapper ciTemplateStagemapper;


}

