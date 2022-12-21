package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.base.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.template.CiTemplateStepCategoryVO;
import io.choerodon.devops.app.service.CiTemplateStepCategoryBusService;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * 流水线步骤模板分类(CiTemplateStepCategory)表控制层
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:21
 */

@RestController("ciProjectTemplateStepCategoryController.v1")
@RequestMapping("/v1/project/{project_id}/ci_template_step_category")
public class CiProjectTemplateStepCategoryController extends BaseController {

    @Autowired
    private CiTemplateStepCategoryBusService ciTemplateStepCategoryBusService;

    @ApiOperation(value = "项目层查询流水线步骤分类列表")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping
    @CustomPageRequest
    public ResponseEntity<Page<CiTemplateStepCategoryVO>> pageTemplateStepCategory(
            @PathVariable(value = "project_id") Long sourceId,
            @ApiParam(value = "分页参数")
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest,
            @RequestParam(value = "name", required = false) String searchParam) {
        return ResponseEntity.ok(ciTemplateStepCategoryBusService.pageTemplateStepCategory(sourceId, pageRequest, searchParam));
    }



}

