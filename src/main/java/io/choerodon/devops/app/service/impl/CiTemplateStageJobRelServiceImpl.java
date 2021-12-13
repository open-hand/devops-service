package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.CiTemplateStageJobRelService;
import io.choerodon.devops.infra.mapper.CiTemplateStageJobRelMapper;

/**
 * 流水线阶段与任务模板的关系表(CiTemplateStageJobRel)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:20
 */
@Service
public class CiTemplateStageJobRelServiceImpl implements CiTemplateStageJobRelService {
    @Autowired
    private CiTemplateStageJobRelMapper ciTemplateStageJobRelmapper;

}

