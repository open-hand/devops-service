package io.choerodon.devops.api.controller.v1;

import org.hzero.core.base.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.devops.app.service.CiTemplateVariableService;

/**
 * 流水线模板配置的CI变量(CiTemplateVariable)表控制层
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:22
 */

@RestController("ciTemplateVariableController.v1")
@RequestMapping("/v1/{organizationId}/ci-template-variables")
public class CiTemplateVariableController extends BaseController {

    @Autowired
    private CiTemplateVariableService ciTemplateVariableService;

}

