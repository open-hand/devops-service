package io.choerodon.devops.api.controller.v1;

import java.util.List;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.base.BaseController;
import org.hzero.core.util.Results;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.template.CiTemplateJobVO;
import io.choerodon.devops.app.service.CiTemplateJobBusService;
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

@RestController("ciSiteTemplateJobController.v1")
@RequestMapping("/v1/site/{source_id}/ci_template_job")
public class CiSiteTemplateJobController extends BaseController {

    @Autowired
    private CiTemplateJobBusService ciTemplateJobBusService;


    @ApiOperation(value = "平台层根据job分组id查询job列表")
    @Permission(level = ResourceLevel.SITE)
    @GetMapping
    public ResponseEntity<List<CiTemplateJobVO>> queryTemplateJobsByGroupId(
            @PathVariable(value = "source_id") Long sourceId,
            @Encrypt @RequestParam(value = "ci_template_job_group_id") Long ciTemplateJobGroupId) {
        return ResponseEntity.ok(ciTemplateJobBusService.queryTemplateJobsByGroupId(sourceId, ciTemplateJobGroupId));
    }

    @ApiOperation(value = "平台层分页查询job列表")
    @Permission(level = ResourceLevel.SITE)
    @GetMapping("/page")
    @CustomPageRequest
    public ResponseEntity<Page<CiTemplateJobVO>> pageTemplateJobs(
            @PathVariable(value = "source_id") Long sourceId,
            @ApiParam(value = "分页参数")
            @ApiIgnore @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest,
            @RequestParam(value = "name", required = false) String name,
            @Encrypt @RequestParam(value = "group_id", required = false) Long groupId,
            @RequestParam(value = "builtIn", required = false) Boolean builtIn,
            @RequestParam(value = "params", required = false) String params) {
        return ResponseEntity.ok(ciTemplateJobBusService.pageTemplateJobs(sourceId, ResourceLevel.SITE.value(), pageRequest, name, groupId, builtIn, params));
    }

    @ApiOperation(value = "平台层层查询job列表")
    @Permission(level = ResourceLevel.SITE)
    @GetMapping("/{template_job_id}")
    public ResponseEntity<CiTemplateJobVO> queryTemplateByJobById(
            @PathVariable(value = "source_id") Long sourceId,
            @Encrypt @PathVariable(value = "template_job_id") Long templateJobId) {
        return ResponseEntity.ok(ciTemplateJobBusService.queryTemplateByJobById(sourceId, templateJobId));
    }

    @ApiOperation(value = "组织层查询job列表")
    @Permission(level = ResourceLevel.SITE)
    @GetMapping("/list")
    public ResponseEntity<List<CiTemplateJobVO>> listTemplateJobs(
            @PathVariable(value = "source_id") Long sourceId) {
        return ResponseEntity.ok(ciTemplateJobBusService.listTemplateJobs(sourceId, ResourceLevel.SITE.value()));
    }


    @ApiOperation(value = "平台层创建job模版")
    @Permission(level = ResourceLevel.SITE)
    @PostMapping
    public ResponseEntity<CiTemplateJobVO> createTemplateJob(
            @PathVariable(value = "source_id") Long sourceId,
            @RequestBody CiTemplateJobVO ciTemplateJobVO) {
        ciTemplateJobVO.setSourceId(0L);
        ciTemplateJobVO.setSourceType(ResourceLevel.SITE.value());
        return ResponseEntity.ok(ciTemplateJobBusService.createTemplateJob(sourceId, ResourceLevel.SITE.value(), ciTemplateJobVO));
    }

    @ApiOperation(value = "平台层更新job模版")
    @Permission(level = ResourceLevel.SITE)
    @PutMapping
    public ResponseEntity<CiTemplateJobVO> updateTemplateJob(
            @PathVariable(value = "source_id") Long sourceId,
            @RequestBody CiTemplateJobVO ciTemplateJobVO) {
        ciTemplateJobVO.setSourceId(0L);
        ciTemplateJobVO.setSourceType(ResourceLevel.SITE.value());
        return ResponseEntity.ok(ciTemplateJobBusService.updateTemplateJob(sourceId, ResourceLevel.SITE.value(), ciTemplateJobVO));
    }

    @ApiOperation(value = "平台层删除job模版")
    @Permission(level = ResourceLevel.SITE)
    @DeleteMapping("/{id}")
    public ResponseEntity<CiTemplateJobVO> deleteTemplateJob(
            @PathVariable(value = "source_id") Long sourceId,
            @Encrypt @PathVariable(value = "id") Long jobId) {
        ciTemplateJobBusService.deleteTemplateJob(sourceId, ResourceLevel.SITE.value(), jobId);
        return Results.success();
    }

    @ApiOperation(value = "平台层校验job名称唯一")
    @Permission(level = ResourceLevel.SITE)
    @GetMapping("/check_name")
    public ResponseEntity<Boolean> isNameUnique(
            @PathVariable(value = "source_id") Long sourceId,
            @Encrypt @RequestParam(value = "job_id", required = false) Long jobId,
            @RequestParam(value = "name") String name) {
        return Results.success(ciTemplateJobBusService.isNameUnique(name, sourceId, jobId));
    }

    @ApiOperation(value = "校验任务是否可以删除（是否关联流水线）")
    @Permission(level = ResourceLevel.SITE)
    @GetMapping("/{template_job_id}/check/delete")
    public ResponseEntity<Boolean> checkJobTemplateByJobId(
            @PathVariable(value = "source_id") Long sourceId,
            @Encrypt @PathVariable(value = "template_job_id") Long templateJobId) {
        return ResponseEntity.ok(ciTemplateJobBusService.checkJobTemplateByJobId(sourceId, templateJobId));
    }

}

