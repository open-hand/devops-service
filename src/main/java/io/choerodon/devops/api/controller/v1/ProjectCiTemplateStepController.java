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
import io.choerodon.devops.api.vo.template.CiTemplateStepVO;
import io.choerodon.devops.app.service.CiTemplateStepService;
import io.choerodon.swagger.annotation.Permission;


@RestController("ProjectCiTemplateStepController.v1")
@RequestMapping("/v1/projects/{project_id}/template_steps")
public class ProjectCiTemplateStepController {

    @Autowired
    private CiTemplateStepService ciTemplateStepService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询项目下的可选的步骤列表")
    @GetMapping
    public ResponseEntity<List<CiTemplateStepVO>> listStepsByTemplateJobId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return ResponseEntity.ok(ciTemplateStepService.listStepsByProjectId(projectId));
    }

}

