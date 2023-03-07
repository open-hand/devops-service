package io.choerodon.devops.api.controller.v1;

import java.util.List;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.base.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.template.CiTemplateJobGroupVO;
import io.choerodon.devops.app.service.CiTemplateJobGroupBusService;
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

@RestController("ciProjectTemplateJobGroupController.v1")
@RequestMapping("/v1/project/{project_id}/ci_template_job_group")
public class CiProjectTemplateJobGroupController extends BaseController {

    @Autowired
    private CiTemplateJobGroupBusService ciTemplateJobGroupBusService;


    @ApiOperation(value = "项目层查询流水线任务分组列表")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping
    @CustomPageRequest
    public ResponseEntity<Page<CiTemplateJobGroupVO>> pageTemplateJobGroup(
            @PathVariable(value = "project_id") Long sourceId,
            @ApiParam(value = "分页参数")
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest,
            @RequestParam(value = "name", required = false) String name) {
        return ResponseEntity.ok(ciTemplateJobGroupBusService.pageTemplateJobGroup(sourceId, pageRequest, name));
    }

    @ApiOperation(value = "项目层查询流水线任务分组列表list")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/list")
    public ResponseEntity<List<CiTemplateJobGroupVO>> listTemplateJobGroup(
            @PathVariable(value = "project_id") Long sourceId,
            @RequestParam(value = "name", required = false) String name) {
        return ResponseEntity.ok(ciTemplateJobGroupBusService.listTemplateJobGroup(sourceId, name));
    }

}

