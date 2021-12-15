package io.choerodon.devops.api.controller.v1;

import java.util.List;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.app.service.CiTemplateJobGroupService;
import io.choerodon.devops.infra.dto.CiTemplateJobGroupDTO;
import io.choerodon.swagger.annotation.Permission;

/**
 * 流水线任务模板分组(CiTemplateJobGroup)表控制层
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:16
 */

@RestController("ciTemplateJobGroupController.v1")
@RequestMapping("/v1/projects/{project_id}/job_groups")
public class ProjectCiTemplateJobGroupController {

    @Autowired
    private CiTemplateJobGroupService ciTemplateJobGroupService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询项目下可用的任务分组")
    @GetMapping
    public ResponseEntity<List<CiTemplateJobGroupDTO>> listGroups(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return ResponseEntity.ok(ciTemplateJobGroupService.listGroups(projectId));
    }
}

