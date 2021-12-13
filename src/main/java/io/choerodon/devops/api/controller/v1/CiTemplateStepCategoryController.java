package io.choerodon.devops.api.controller.v1;

import org.hzero.core.base.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.devops.app.service.CiTemplateStepCategoryService;

/**
 * 流水线步骤模板分类(CiTemplateStepCategory)表控制层
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:21
 */

@RestController("ciTemplateStepCategoryController.v1")
@RequestMapping("/v1/{organizationId}/ci-template-step-categorys")
public class CiTemplateStepCategoryController extends BaseController {

    @Autowired
    private CiTemplateStepCategoryService ciTemplateStepCategoryService;

}

