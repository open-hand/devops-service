package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.CiTemplateJobService;
import io.choerodon.devops.infra.mapper.CiTemplateJobMapper;

/**
 * 流水线任务模板表(CiTemplateJob)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:16
 */
@Service
public class CiTemplateJobServiceImpl implements CiTemplateJobService {
    @Autowired
    private CiTemplateJobMapper ciTemplateJobmapper;


}

