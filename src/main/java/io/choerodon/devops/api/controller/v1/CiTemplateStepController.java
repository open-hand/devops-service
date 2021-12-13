package io.choerodon.devops.api.controller.v1;

import org.hzero.core.base.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.devops.app.service.CiTemplateStepService;

/**
 * 流水线步骤模板(CiTemplateStep)表控制层
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:21
 */

@RestController("ciTemplateStepController.v1")
@RequestMapping("/v1/{organizationId}/ci-template-steps")
public class CiTemplateStepController extends BaseController {

    @Autowired
    private CiTemplateStepService ciTemplateStepService;

}

