package io.choerodon.devops.api.controller.v1;

import java.util.List;
import javax.validation.Valid;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.template.CiTemplateStepCategoryVO;
import io.choerodon.devops.api.vo.template.CiTemplateStepVO;
import io.choerodon.devops.app.service.CiTemplateStepBusService;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * Created by wangxiang on 2021/12/14
 */
@RestController("ciSiteTemplateStepController.v1")
@RequestMapping("/v1/site/{source_id}/ci_template_step")
public class CiSiteTemplateStepController {


    @Autowired
    private CiTemplateStepBusService ciTemplateStepBusService;

    @ApiOperation(value = "平台层查询流水线步骤模板")
    @Permission(level = ResourceLevel.SITE)
    @GetMapping
    @CustomPageRequest
    public ResponseEntity<Page<CiTemplateStepVO>> pageTemplateStep(
            @PathVariable(value = "source_id") Long sourceId,
            @ApiParam(value = "分页参数")
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest,
            @RequestParam(value = "name", required = false) String name,
            @Encrypt @RequestParam(value = "category_id", required = false) Long categoryId,
            @RequestParam(value = "builtIn", required = false) Boolean builtIn,
            @RequestParam(value = "params", required = false) String params) {
        return ResponseEntity.ok(ciTemplateStepBusService.pageTemplateStep(sourceId, ResourceLevel.SITE.value(), pageRequest, name, categoryId, builtIn, params));
    }


    @ApiOperation(value = "平台层查询步骤模板列表")
    @Permission(level = ResourceLevel.SITE)
    @GetMapping("/list")
    public ResponseEntity<List<CiTemplateStepVO>> templateStepList(
            @PathVariable(value = "source_id") Long sourceId,
            @RequestParam(value = "name", required = false) String name) {
        return ResponseEntity.ok(ciTemplateStepBusService.templateStepList(sourceId, ResourceLevel.SITE.value(), name));
    }

    @Permission(level = ResourceLevel.SITE)
    @ApiOperation(value = "平台项目下的可选的步骤列表")
    @GetMapping("/list/with/category")
    public ResponseEntity<List<CiTemplateStepCategoryVO>> listStepWithCategory(
            @ApiParam(value = "source_id", required = true)
            @PathVariable(value = "source_id") Long sourceId) {
        return ResponseEntity.ok(ciTemplateStepBusService.listStepWithCategory(sourceId, ResourceLevel.SITE.value()));
    }


    @ApiOperation(value = "平台层修改流水线步骤模板")
    @Permission(level = ResourceLevel.SITE)
    @PutMapping
    public ResponseEntity<CiTemplateStepVO> updateTemplateStep(
            @PathVariable(value = "source_id") Long sourceId,
            @RequestBody CiTemplateStepVO ciTemplateStepVO) {
        return ResponseEntity.ok(ciTemplateStepBusService.updateTemplateStep(sourceId, ciTemplateStepVO));
    }

    @ApiOperation(value = "平台层删除流水线步骤模板")
    @Permission(level = ResourceLevel.SITE)
    @DeleteMapping("/{template_step_id}")
    public ResponseEntity<Void> deleteTemplateStep(
            @PathVariable(value = "source_id") Long sourceId,
            @Encrypt @PathVariable("template_step_id") Long ciStepTemplateId) {
        ciTemplateStepBusService.deleteTemplateStep(sourceId, ResourceLevel.SITE.value(), ciStepTemplateId);
        return ResponseEntity.noContent().build();
    }


    @ApiOperation(value = "平台层创建流水线步骤模板")
    @Permission(level = ResourceLevel.SITE)
    @PostMapping
    public ResponseEntity<CiTemplateStepVO> createTemplateStep(
            @PathVariable(value = "source_id") Long sourceId,
            @RequestBody @Valid CiTemplateStepVO ciTemplateStepVO) {
        ciTemplateStepVO.setVisibility(true);
        return ResponseEntity.ok(ciTemplateStepBusService.createTemplateStep(sourceId, ciTemplateStepVO));
    }

    @ApiOperation(value = "平台层根据jobId查询步骤模板")
    @Permission(level = ResourceLevel.SITE)
    @GetMapping("/template_job_id/{template_job_id}")
    public ResponseEntity<List<CiTemplateStepVO>> queryStepTemplateByJobId(
            @PathVariable(value = "source_id") Long sourceId,
            @Encrypt @PathVariable(value = "template_job_id") Long templateJobId) {
        return ResponseEntity.ok(ciTemplateStepBusService.queryStepTemplateByJobId(sourceId, templateJobId));
    }

    @ApiOperation(value = "平台层根据stepId查询步骤模板")
    @Permission(level = ResourceLevel.SITE)
    @GetMapping("/template_step_id/{template_step_id}")
    public ResponseEntity<CiTemplateStepVO> queryStepTemplateByStepId(
            @PathVariable(value = "source_id") Long sourceId,
            @Encrypt @PathVariable(value = "template_step_id") Long templateStepId) {
        return ResponseEntity.ok(ciTemplateStepBusService.queryStepTemplateByStepId(sourceId, templateStepId));
    }

    @ApiOperation(value = "校验步骤是否可以删除（是否关联流水线）")
    @Permission(level = ResourceLevel.SITE)
    @GetMapping("/{template_step_id}/check/delete")
    public ResponseEntity<Boolean> checkStepTemplateByStepId(
            @PathVariable(value = "source_id") Long sourceId,
            @Encrypt @PathVariable(value = "template_step_id") Long templateStepId) {
        return ResponseEntity.ok(ciTemplateStepBusService.checkStepTemplateByStepId(sourceId, templateStepId));
    }


    @ApiOperation(value = "平台层校验步骤名称是否唯一")
    @Permission(level = ResourceLevel.SITE)
    @GetMapping("/check/name/unique")
    public ResponseEntity<Boolean> checkTemplateStepName(
            @PathVariable(value = "source_id") Long sourceId,
            @RequestParam(value = "name") String name,
            @Encrypt @RequestParam(value = "template_step_id", required = false) Long templateStepId) {
        return ResponseEntity.ok(ciTemplateStepBusService.checkTemplateStepName(sourceId, name, templateStepId));
    }

}
