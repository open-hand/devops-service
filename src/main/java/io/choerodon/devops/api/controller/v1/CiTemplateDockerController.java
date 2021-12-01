package io.choerodon.devops.api.controller.v1;

import org.hzero.core.base.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.devops.app.service.CiTemplateDockerService;

/**
 * 流水线任务模板与步骤模板关系表(CiTemplateDocker)表控制层
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:56:48
 */

@RestController("ciTemplateDockerController.v1")
@RequestMapping("/v1/{organizationId}/ci-template-dockers")
public class CiTemplateDockerController extends BaseController {

    @Autowired
    private CiTemplateDockerService ciTemplateDockerService;

}

