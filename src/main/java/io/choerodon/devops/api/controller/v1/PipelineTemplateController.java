package io.choerodon.devops.api.controller.v1;

import org.hzero.core.base.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.devops.app.service.PipelineTemplateService;

/**
 * 流水线模板表(PipelineTemplate)表控制层
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 16:54:20
 */

@RestController("pipelineTemplateController.v1")
@RequestMapping("/v1/{organizationId}/pipeline-templates")
public class PipelineTemplateController extends BaseController {

    @Autowired
    private PipelineTemplateService pipelineTemplateService;

}

