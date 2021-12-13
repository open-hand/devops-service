package io.choerodon.devops.api.controller.v1;

import org.hzero.core.base.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.devops.app.service.CiTemplateJobGroupService;

/**
 * 流水线任务模板分组(CiTemplateJobGroup)表控制层
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:16
 */

@RestController("ciTemplateJobGroupController.v1")
@RequestMapping("/v1/{organizationId}/ci-template-job-groups")
public class CiTemplateJobGroupController extends BaseController {

    @Autowired
    private CiTemplateJobGroupService ciTemplateJobGroupService;

}

