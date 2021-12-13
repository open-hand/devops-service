package io.choerodon.devops.api.controller.v1;

import org.hzero.core.base.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.devops.app.service.CiTemplateStageService;

/**
 * 流水线模阶段(CiTemplateStage)表控制层
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:20
 */

@RestController("ciTemplateStageController.v1")
@RequestMapping("/v1/{organizationId}/ci-template-stages")
public class CiTemplateStageController extends BaseController {

    @Autowired
    private CiTemplateStageService ciTemplateStageService;

}

