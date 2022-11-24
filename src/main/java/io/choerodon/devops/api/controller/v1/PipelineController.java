package io.choerodon.devops.api.controller.v1;

import org.hzero.core.base.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 流水线表(Pipeline)表控制层
 *
 * @author
 * @since 2022-11-24 15:50:13
 */

@RestController("pipelineController.v1")
@RequestMapping("/v1/{organizationId}/pipelines")
public class PipelineController extends BaseController {


}

