package io.choerodon.devops.api.controller.v1;

import io.choerodon.devops.app.service.DevopsCiContentService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:30
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/contents")
public class DevopsCiContentController {
    private DevopsCiContentService devopsCiContentService;

    public DevopsCiContentController(DevopsCiContentService devopsCiContentService) {
        this.devopsCiContentService = devopsCiContentService;
    }
}
