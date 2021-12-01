package io.choerodon.devops.api.controller.v1;

import org.hzero.core.base.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.devops.app.service.CiTemplateSonarService;

/**
 * devops_ci_template_sonar(CiTemplateSonar)表控制层
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:19
 */

@RestController("ciTemplateSonarController.v1")
@RequestMapping("/v1/{organizationId}/ci-template-sonars")
public class CiTemplateSonarController extends BaseController {

    @Autowired
    private CiTemplateSonarService ciTemplateSonarService;

}

