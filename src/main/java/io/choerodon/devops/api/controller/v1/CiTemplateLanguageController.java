package io.choerodon.devops.api.controller.v1;

import org.hzero.core.base.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.devops.app.service.CiTemplateCategoryService;

/**
 * 流水线模板适用语言表(CiTemplateLanguage)表控制层
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:18
 */

@RestController("ciTemplateLanguageController.v1")
@RequestMapping("/v1/{organizationId}/ci-template-languages")
public class CiTemplateLanguageController extends BaseController {

    @Autowired
    private CiTemplateCategoryService ciTemplateCategoryService;

}

