package io.choerodon.devops.api.controller.v1;

import org.hzero.core.base.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.devops.app.service.CiTemplateStageJobRelService;

/**
 * 流水线阶段与任务模板的关系表(CiTemplateStageJobRel)表控制层
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:21
 */

@RestController("ciTemplateStageJobRelController.v1")
@RequestMapping("/v1/{organizationId}/ci-template-stage-job-rels")
public class CiTemplateStageJobRelController extends BaseController {

    @Autowired
    private CiTemplateStageJobRelService ciTemplateStageJobRelService;

}

