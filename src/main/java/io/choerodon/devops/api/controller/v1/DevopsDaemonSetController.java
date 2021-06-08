package io.choerodon.devops.api.controller.v1;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/v1/projects/{project_id}/daemon_sets")
public class DevopsDaemonSetController {
}
