package io.choerodon.devops.api.controller.v1;

import io.choerodon.devops.app.service.DevopsCiJobRecordService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:33
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/ci_job_records")
public class DevopsCiJobRecordController {
    private DevopsCiJobRecordService devopsCiJobRecordService;

    public DevopsCiJobRecordController(DevopsCiJobRecordService devopsCiJobRecordService) {
        this.devopsCiJobRecordService = devopsCiJobRecordService;
    }
}
