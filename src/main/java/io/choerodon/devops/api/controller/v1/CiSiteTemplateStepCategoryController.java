package io.choerodon.devops.api.controller.v1;

import javax.validation.Valid;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.base.BaseController;
import org.hzero.starter.keyencrypt.core.Encrypt;
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

@RestController("ciSiteTemplateStepCategoryController.v1")
@RequestMapping("/v1/site/{source_id}/ci_template_step_category")
public class CiSiteTemplateStepCategoryController extends BaseController {

    @Autowired
    private CiTemplateStepCategoryBusService ciTemplateStepCategoryBusService;

    @ApiOperation(value = "平台层查询流水线步骤分类列表")
    @Permission(level = ResourceLevel.SITE)
    @GetMapping
    @CustomPageRequest
    public ResponseEntity<Page<CiTemplateStepCategoryVO>> pageTemplateStepCategory(
            @PathVariable(value = "source_id") Long sourceId,
            @ApiParam(value = "分页参数")
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest,
            @RequestParam(value = "name", required = false) String name) {
        return ResponseEntity.ok(ciTemplateStepCategoryBusService.pageTemplateStepCategory(sourceId, pageRequest, name));
    }


    @ApiOperation(value = "平台层创建流水线步骤模板分类")
    @Permission(level = ResourceLevel.SITE)
    @PostMapping
    public ResponseEntity<CiTemplateStepCategoryVO> createTemplateStepCategory(
            @PathVariable(value = "source_id") Long sourceId,
            @RequestBody @Valid CiTemplateStepCategoryVO CiTemplateStepCategoryVO) {
        return ResponseEntity.ok(ciTemplateStepCategoryBusService.createTemplateStepCategory(sourceId, CiTemplateStepCategoryVO));
    }

    @ApiOperation(value = "平台层修改流水线步骤分类模板")
    @Permission(level = ResourceLevel.SITE)
    @PutMapping
    public ResponseEntity<CiTemplateStepCategoryVO> updateTemplateStepCategory(
            @PathVariable(value = "source_id") Long sourceId,
            @RequestBody CiTemplateStepCategoryVO ciTemplateStepCategoryVO) {
        return ResponseEntity.ok(ciTemplateStepCategoryBusService.updateTemplateStepCategory(sourceId, ciTemplateStepCategoryVO));
    }

    @ApiOperation(value = "平台层删除流水线步骤分类模板")
    @Permission(level = ResourceLevel.SITE)
    @DeleteMapping("/{ci_template_category_id}")
    public ResponseEntity<Void> deleteTemplateStepCategory(
            @PathVariable(value = "source_id") Long sourceId,
            @Encrypt @PathVariable(value = "ci_template_category_id") Long ciTemplateCategoryId) {
        ciTemplateStepCategoryBusService.deleteTemplateStepCategory(sourceId, ciTemplateCategoryId);
        return ResponseEntity.noContent().build();
    }


    @ApiOperation(value = "平台层校验分类名称是否唯一")
    @Permission(level = ResourceLevel.SITE)
    @GetMapping("/check/name/unique")
    public ResponseEntity<Boolean> checkTemplateStepCategory(
            @PathVariable(value = "source_id") Long sourceId,
            @RequestParam(value = "name", required = false) String name,
            @Encrypt @RequestParam(value = "ci_template_category_id",required = false) Long ciTemplateCategoryId) {
        return ResponseEntity.ok(ciTemplateStepCategoryBusService.checkTemplateStepCategory(sourceId, name, ciTemplateCategoryId));
    }

}

