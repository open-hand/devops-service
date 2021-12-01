package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.CiTemplateJobGroupService;
import io.choerodon.devops.infra.mapper.CiTemplateJobGroupMapper;

/**
 * 流水线任务模板分组(CiTemplateJobGroup)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:16
 */
@Service
public class CiTemplateJobGroupServiceImpl implements CiTemplateJobGroupService {
    @Autowired
    private CiTemplateJobGroupMapper ciTemplateJobGroupmapper;


}

