package io.choerodon.devops.api.controller.v1;

import java.util.List;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.app.service.CiTemplateVariableService;
import io.choerodon.devops.infra.dto.CiTemplateVariableDTO;
import io.choerodon.swagger.annotation.Permission;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/12/22 10:59
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/ci_template_variables")
public class ProjectCiTemplateVariableController {

    @Autowired
    private CiTemplateVariableService ciTemplateVariableService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "根据模板d查询环境变量信息")
    @GetMapping
    public ResponseEntity<List<CiTemplateVariableDTO>> listByPipelineId(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt(ignoreUserConflict = true)
            @ApiParam(value = "流水线Id", required = true)
            @RequestParam(value = "template_id") Long templateId) {
        return ResponseEntity.ok(ciTemplateVariableService.listByTemplateId(templateId));
    }
}
