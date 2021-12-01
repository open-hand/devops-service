package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.CiTemplateStepCategoryService;
import io.choerodon.devops.infra.mapper.CiTemplateStepCategoryMapper;

/**
 * 流水线步骤模板分类(CiTemplateStepCategory)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:21
 */
@Service
public class CiTemplateStepCategoryServiceImpl implements CiTemplateStepCategoryService {
    @Autowired
    private CiTemplateStepCategoryMapper ciTemplateStepCategorymapper;


}

