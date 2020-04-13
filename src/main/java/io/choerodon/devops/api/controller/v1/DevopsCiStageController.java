package io.choerodon.devops.api.controller.v1;

import io.choerodon.devops.app.service.DevopsCiStageService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:28
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/ci_stages")
public class DevopsCiStageController {
    private DevopsCiStageService devopsCiStageService;

    public DevopsCiStageController(DevopsCiStageService devopsCiStageService) {
        this.devopsCiStageService = devopsCiStageService;
    }
}
