package io.choerodon.devops.api.controller.v1;


import java.util.List;

import io.choerodon.devops.api.vo.template.DeleteCheckResultVO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.template.CiTemplateJobVO;
import io.choerodon.devops.app.service.CiTemplateJobBusService;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * 组织层流水线任务模板(CiTemplateJob)表控制层
 *
 * @author lihao
 * @since 2021-12-01 15:58:16
 */

@RestController("ciProjectPipelineTemplateController.v1")
@RequestMapping("/v1/project/{project_id}/ci_template_job")
public class CiProjectTemplateJobController {

    @Autowired
    private CiTemplateJobBusService ciTemplateJobBusService;

    @ApiOperation("项目层查询job模版")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @CustomPageRequest
    @GetMapping("/page")
    public ResponseEntity<Page<CiTemplateJobVO>> pageTemplateJob(
            @PathVariable("project_id") Long resourceId,
            @ApiParam(value = "分页参数")
            @ApiIgnore @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest,
            @RequestParam(value = "name", required = false) String name,
            @Encrypt @RequestParam(value = "group_id", required = false) Long groupId,
            @RequestParam(value = "builtIn", required = false) Boolean builtIn,
            @RequestParam(value = "params", required = false) String params) {
        return Results.success(ciTemplateJobBusService.pageTemplateJobs(resourceId, ResourceLevel.PROJECT.value(), pageRequest, name, groupId, builtIn, params));
    }

    @ApiOperation(value = "项目层层查询job列表")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/list")
    public ResponseEntity<List<CiTemplateJobVO>> listTemplateJobs(
            @PathVariable(value = "project_id") Long sourceId) {
        return ResponseEntity.ok(ciTemplateJobBusService.listTemplateJobs(sourceId, ResourceLevel.PROJECT.value()));
    }

    @ApiOperation(value = "项目层查询job列表")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{template_job_id}")
    public ResponseEntity<CiTemplateJobVO> queryTemplateByJobById(
            @PathVariable(value = "project_id") Long sourceId,
            @Encrypt @PathVariable(value = "template_job_id") Long templateJobId) {
        return ResponseEntity.ok(ciTemplateJobBusService.queryTemplateByJobById(sourceId, templateJobId));
    }

    @ApiOperation(value = "项目层创建job模版")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping
    public ResponseEntity<CiTemplateJobVO> createTemplateJob(
            @PathVariable(value = "project_id") Long sourceId,
            @RequestBody CiTemplateJobVO ciTemplateJobVO) {
        if (sourceId == 0) {
            throw new CommonException("error.invalid.sourceId");
        }
        ciTemplateJobVO.setSourceType(ResourceLevel.PROJECT.value());
        return ResponseEntity.ok(ciTemplateJobBusService.createTemplateJob(sourceId, ResourceLevel.PROJECT.value(), ciTemplateJobVO));
    }

    @ApiOperation(value = "项目层更新job模版")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PutMapping
    public ResponseEntity<CiTemplateJobVO> updateTemplateJob(
            @PathVariable(value = "project_id") Long sourceId,
            @RequestBody CiTemplateJobVO ciTemplateJobVO) {
        if (sourceId == 0) {
            throw new CommonException("error.invalid.sourceId");
        }
        ciTemplateJobVO.setSourceType(ResourceLevel.PROJECT.value());
        return ResponseEntity.ok(ciTemplateJobBusService.updateTemplateJob(sourceId, ResourceLevel.PROJECT.value(), ciTemplateJobVO));
    }

    @ApiOperation(value = "项目层删除job模版")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping("/{job_id}")
    public ResponseEntity<CiTemplateJobVO> deleteTemplateJob(
            @PathVariable(value = "project_id") Long sourceId,
            @Encrypt @PathVariable(value = "job_id") Long jobId) {
        if (sourceId == 0) {
            throw new CommonException("error.invalid.sourceId");
        }
        ciTemplateJobBusService.deleteTemplateJob(sourceId, ResourceLevel.PROJECT.value(), jobId);
        return Results.success();
    }

    @ApiOperation(value = "项目层校验job名称唯一")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/check_name")
    public ResponseEntity<Boolean> isNameUnique(
            @PathVariable(value = "project_id") Long sourceId,
            @Encrypt @RequestParam(value = "job_id", required = false) Long jobId,
            @RequestParam(value = "name") String name) {
        return Results.success(ciTemplateJobBusService.isNameUnique(name, sourceId, jobId));
    }

    @ApiOperation(value = "校验任务是否可以删除（是否关联流水线）")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{template_job_id}/check/delete")
    public ResponseEntity<DeleteCheckResultVO> checkJobTemplateByJobId(
            @PathVariable(value = "project_id") Long sourceId,
            @Encrypt @PathVariable(value = "template_job_id") Long templateJobId) {
        return ResponseEntity.ok(ciTemplateJobBusService.checkJobTemplateByJobId(sourceId, ResourceLevel.PROJECT.value(), templateJobId));
    }

    @ApiOperation(value = "项目层根据job分组id查询job列表")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping
    public ResponseEntity<List<CiTemplateJobVO>> queryTemplateJobsByGroupId(
            @PathVariable(value = "project_id") Long sourceId,
            @Encrypt @RequestParam(value = "ci_template_job_group_id") Long ciTemplateJobGroupId) {
        return ResponseEntity.ok(ciTemplateJobBusService.queryTemplateJobsByGroupId(sourceId, ciTemplateJobGroupId));
    }
}
