package io.choerodon.devops.api.controller.v1;

import io.choerodon.devops.app.service.DevopsCiJobService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:29
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/ci_jobs")
public class DevopsCiJobController {
    private DevopsCiJobService devopsCiJobService;

    public DevopsCiJobController(DevopsCiJobService devopsCiJobService) {
        this.devopsCiJobService = devopsCiJobService;
    }
}
