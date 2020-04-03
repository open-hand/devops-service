package io.choerodon.devops.api.controller.v1;

import io.choerodon.devops.app.service.DevopsCiPipelineService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 〈功能简述〉
 * 〈CI流水线Controller〉
 *
 * @author wanghao
 * @Date 2020/4/2 17:57
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/ci_pipelines")
public class DevopsCiPipelineController {

    private DevopsCiPipelineService devopsCiPipelineService;

    public DevopsCiPipelineController(DevopsCiPipelineService devopsCiPipelineService) {
        this.devopsCiPipelineService = devopsCiPipelineService;
    }
}
