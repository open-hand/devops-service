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
@RestController("ciProjectTemplateStepController.v1")
@RequestMapping("/v1/project/{project_id}/ci_template_step")
public class CiProjectTemplateStepController {


    @Autowired
    private CiTemplateStepBusService ciTemplateStepBusService;

    @ApiOperation(value = "项目层查询流水线步骤模板")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping
    @CustomPageRequest
    public ResponseEntity<Page<CiTemplateStepVO>> pageTemplateStep(
            @PathVariable(value = "project_id") Long sourceId,
            @ApiParam(value = "分页参数")
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest,
            @RequestParam(value = "name", required = false) String name,
            @Encrypt @RequestParam(value = "category_id", required = false) Long categoryId,
            @RequestParam(value = "builtIn", required = false) Boolean builtIn,
            @RequestParam(value = "params", required = false) String params) {
        return ResponseEntity.ok(ciTemplateStepBusService.pageTemplateStep(sourceId, ResourceLevel.PROJECT.value(), pageRequest, name, categoryId, builtIn, params));
    }


    @ApiOperation(value = "项目层查询步骤模板列表")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/list")
    public ResponseEntity<List<CiTemplateStepVO>> templateStepList(
            @PathVariable(value = "project_id") Long sourceId,
            @RequestParam(value = "name", required = false) String name) {
        return ResponseEntity.ok(ciTemplateStepBusService.templateStepList(sourceId, ResourceLevel.PROJECT.value(), name));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "项目层可选的步骤列表")
    @GetMapping("/list/with/category")
    public ResponseEntity<List<CiTemplateStepCategoryVO>> listStepWithCategory(
            @ApiParam(value = "project_id", required = true)
            @PathVariable(value = "project_id") Long sourceId) {
        return ResponseEntity.ok(ciTemplateStepBusService.listStepWithCategory(sourceId, ResourceLevel.PROJECT.value()));
    }


    @ApiOperation(value = "项目层修改流水线步骤模板")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PutMapping
    public ResponseEntity<CiTemplateStepVO> updateTemplateStep(
            @PathVariable(value = "project_id") Long sourceId,
            @RequestBody CiTemplateStepVO ciTemplateStepVO) {
        return ResponseEntity.ok(ciTemplateStepBusService.updateTemplateStep(sourceId, ciTemplateStepVO));
    }

    @ApiOperation(value = "项目层删除流水线步骤模板")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping("/{ci_step_template_id}")
    public ResponseEntity<Void> deleteTemplateStep(
            @PathVariable(value = "project_id") Long sourceId,
            @Encrypt @PathVariable("ci_step_template_id") Long ciStepTemplateId) {
        ciTemplateStepBusService.deleteTemplateStep(sourceId, ResourceLevel.PROJECT.value(), ciStepTemplateId);
        return ResponseEntity.noContent().build();
    }


    @ApiOperation(value = "项目层创建流水线步骤模板")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping
    public ResponseEntity<CiTemplateStepVO> createTemplateStep(
            @PathVariable(value = "project_id") Long sourceId,
            @RequestBody @Valid CiTemplateStepVO ciTemplateStepVO) {
        ciTemplateStepVO.setVisibility(true);
        return ResponseEntity.ok(ciTemplateStepBusService.createTemplateStep(sourceId, ciTemplateStepVO));
    }

    @ApiOperation(value = "项目层根据jobId查询步骤模板")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/template_job_id/{template_job_id}")
    public ResponseEntity<List<CiTemplateStepVO>> queryStepTemplateByJobId(
            @PathVariable(value = "project_id") Long sourceId,
            @Encrypt @PathVariable(value = "template_job_id") Long templateJobId) {
        return ResponseEntity.ok(ciTemplateStepBusService.queryStepTemplateByJobId(sourceId, templateJobId));
    }

    @ApiOperation(value = "项目层根据stepId查询步骤模板")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/template_step_id/{template_step_id}")
    public ResponseEntity<CiTemplateStepVO> queryStepTemplateByStepId(
            @PathVariable(value = "project_id") Long sourceId,
            @Encrypt @PathVariable(value = "template_step_id") Long templateStepId) {
        return ResponseEntity.ok(ciTemplateStepBusService.queryStepTemplateByStepId(sourceId, templateStepId));
    }

    @ApiOperation(value = "项目层校验步骤是否可以删除（是否关联流水线）")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{template_step_id}/check/delete")
    public ResponseEntity<Boolean> checkStepTemplateByStepId(
            @PathVariable(value = "project_id") Long sourceId,
            @Encrypt @PathVariable(value = "template_step_id") Long templateStepId) {
        return ResponseEntity.ok(ciTemplateStepBusService.checkStepTemplateByStepId(sourceId, templateStepId));
    }

    @ApiOperation(value = "项目层校验步骤名称是否唯一")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/check/name/unique")
    public ResponseEntity<Boolean> checkTemplateStepName(
            @PathVariable(value = "project_id") Long sourceId,
            @RequestParam(value = "name") String name,
            @Encrypt @RequestParam(value = "template_step_id", required = false) Long templateStepId) {
        return ResponseEntity.ok(ciTemplateStepBusService.checkTemplateStepName(sourceId, name, templateStepId));
    }
}
