package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.base.BaseController;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.template.CiTemplatePipelineVO;
import io.choerodon.devops.app.service.CiPipelineTemplateBusService;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * 流水线任务模板分组(CiTemplateJobGroup)表控制层
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:16
 */

@RestController("ciProjectTemplatePipelineController.v1")
@RequestMapping("/v1/project/{project_id}/ci_template_pipeline")
public class CiProjectTemplatePipelineController extends BaseController {

    @Autowired
    private CiPipelineTemplateBusService ciPipelineTemplateBusService;


    @ApiOperation(value = "项目层查询流水线模板")
    @GetMapping
    @CustomPageRequest
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<Page<CiTemplatePipelineVO>> pagePipelineTemplate(
            @PathVariable(value = "project_id") Long sourceId,
            @ApiParam(value = "分页参数")
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest,
            @RequestParam(value = "name", required = false) String name,
            @Encrypt @RequestParam(value = "category_id", required = false) Long categoryId,
            @RequestParam(value = "builtIn", required = false) Boolean builtIn,
            @RequestParam(value = "enable", required = false) Boolean enable,
            @RequestParam(value = "params", required = false) String params) {
        return ResponseEntity.ok(ciPipelineTemplateBusService.pagePipelineTemplate(sourceId, ResourceLevel.PROJECT.value(), pageRequest, name, categoryId, builtIn, enable, params));
    }

    @ApiOperation(value = "项目层创建流水线模板")
    @PostMapping
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<CiTemplatePipelineVO> createPipelineTemplate(
            @PathVariable(value = "project_id") Long sourceId,
            @Validated @RequestBody CiTemplatePipelineVO ciTemplatePipelineVO) {
        return ResponseEntity.ok(ciPipelineTemplateBusService.createPipelineTemplate(sourceId, ResourceLevel.PROJECT.value(), ciTemplatePipelineVO));
    }

    @ApiOperation(value = "项目层根据流水线模板id查询模板")
    @GetMapping("/{ci_template_id}")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<CiTemplatePipelineVO> queryPipelineTemplateById(
            @PathVariable(value = "project_id") Long sourceId,
            @Encrypt @PathVariable(value = "ci_template_id") Long ciPipelineTemplateId) {
        return ResponseEntity.ok(ciPipelineTemplateBusService.queryPipelineTemplateById(sourceId, ciPipelineTemplateId));
    }

    @ApiOperation(value = "项目层修改流水线模板")
    @PutMapping
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<CiTemplatePipelineVO> updatePipelineTemplate(
            @PathVariable(value = "project_id") Long sourceId,
            @RequestBody CiTemplatePipelineVO devopsPipelineTemplateVO) {
        return ResponseEntity.ok(ciPipelineTemplateBusService.updatePipelineTemplate(sourceId, ResourceLevel.PROJECT.value(), devopsPipelineTemplateVO));
    }


    @ApiOperation(value = "项目层停用流水线模板")
    @PutMapping("/invalid")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<Void> invalidPipelineTemplate(
            @PathVariable(value = "project_id") Long sourceId,
            @Encrypt @RequestParam(value = "ci_pipeline_template_id") Long ciPipelineTemplateId) {
        ciPipelineTemplateBusService.invalidPipelineTemplate(sourceId, ciPipelineTemplateId);
        return ResponseEntity.noContent().build();
    }


    @ApiOperation(value = "项目层启用流水线模板")
    @PutMapping("/enable")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<Void> enablePipelineTemplate(
            @PathVariable(value = "project_id") Long sourceId,
            @Encrypt @RequestParam(value = "ci_pipeline_template_id") Long ciPipelineTemplateId) {
        ciPipelineTemplateBusService.enablePipelineTemplate(sourceId, ciPipelineTemplateId);
        return ResponseEntity.noContent().build();
    }


    @ApiOperation(value = "项目层流水线重名校验")
    @GetMapping("/check/name/unique")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<Boolean> checkPipelineTemplateName(
            @PathVariable(value = "project_id") Long organizationId,
            @RequestParam(value = "name") String name,
            @Encrypt @RequestParam(value = "ci_pipeline_template_id", required = false) Long ciPipelineTemplateId) {
        return ResponseEntity.ok(ciPipelineTemplateBusService.checkPipelineTemplateName(organizationId, name, ciPipelineTemplateId));
    }


    @ApiOperation(value = "项目层删除流水线模板")
    @DeleteMapping("/{ci_template_pipeline_id}")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<Void> deletePipelineTemplate(
            @PathVariable(value = "project_id") Long sourceId,
            @Encrypt @PathVariable(value = "ci_template_pipeline_id") Long ciTemplatePipelineId) {
        ciPipelineTemplateBusService.deletePipelineTemplate(sourceId, ciTemplatePipelineId);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "项目层流水线保存为流水线模板")
    @PostMapping("/save")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<CiTemplatePipelineVO> savePipelineTemplate(
            @PathVariable(value = "project_id") Long sourceId,
            @Validated @RequestBody CiTemplatePipelineVO CiTemplatePipelineVO) {
        return ResponseEntity.ok(ciPipelineTemplateBusService.savePipelineTemplate(sourceId, CiTemplatePipelineVO));
    }

}

