package io.choerodon.devops.api.controller.v1;

import org.hzero.core.base.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 流水线执行记录(PipelineRecord)表控制层
 *
 * @author
 * @since 2022-11-23 16:43:02
 */

@RestController("pipelineRecordController.v1")
@RequestMapping("/v1/projects/{projectId}/pipeline_records")
public class PipelineRecordController extends BaseController {

}

