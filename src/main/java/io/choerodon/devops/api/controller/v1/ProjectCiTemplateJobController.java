package io.choerodon.devops.api.controller.v1;

import java.util.List;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.base.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.template.CiTemplateJobVO;
import io.choerodon.devops.app.service.CiTemplateJobService;
import io.choerodon.swagger.annotation.Permission;

/**
 * 流水线任务模板表(CiTemplateJob)表控制层
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:16
 */

@RestController("ciTemplateJobController.v1")
@RequestMapping("/v1/projects/{project_id}/template_jobs")
public class ProjectCiTemplateJobController extends BaseController {

    @Autowired
    private CiTemplateJobService ciTemplateJobService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询分组下的任务列表")
    @GetMapping
    public ResponseEntity<List<CiTemplateJobVO>> listJobsByGroupId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @RequestParam(value = "group_id") Long groupId) {
        return ResponseEntity.ok(ciTemplateJobService.listJobsByGroupId(projectId, groupId));
    }

}

