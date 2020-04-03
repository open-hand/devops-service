package io.choerodon.devops.api.controller.v1;

import io.choerodon.devops.app.service.DevopsCiPipelineRecordService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:31
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/ci_pipeline_records")
public class DevopsCiPipelineRecordController {
    private DevopsCiPipelineRecordService devopsCiPipelineRecordService;

    public DevopsCiPipelineRecordController(DevopsCiPipelineRecordService devopsCiPipelineRecordService) {
        this.devopsCiPipelineRecordService = devopsCiPipelineRecordService;
    }
}
