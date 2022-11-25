package io.choerodon.devops.api.controller.v1;

import javax.validation.Valid;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.base.BaseController;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.PipelineVO;
import io.choerodon.devops.app.service.PipelineService;
import io.choerodon.devops.infra.dto.PipelineDTO;
import io.choerodon.swagger.annotation.Permission;

/**
 * 流水线表(Pipeline)表控制层
 *
 * @author
 * @since 2022-11-24 15:50:13
 */

@RestController("pipelineController.v1")
@RequestMapping("/v1/projects/{project_id}/pipelines")
public class PipelineController extends BaseController {

    @Autowired
    private PipelineService pipelineService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "项目下创建自动化部署流水线")
    @PostMapping
    public ResponseEntity<PipelineDTO> create(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @RequestBody @Valid PipelineVO pipelineVO) {
        return ResponseEntity.ok(pipelineService.create(projectId, pipelineVO));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "启用自动化部署流水线")
    @PutMapping("/{id}/enable")
    public ResponseEntity<Void> enable(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @PathVariable(value = "id") Long id) {
        pipelineService.enable(projectId, id);
        return ResponseEntity.noContent().build();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "停用自动化部署流水线")
    @PutMapping("/{id}/disable")
    public ResponseEntity<Void> disable(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @PathVariable(value = "id") Long id) {
        pipelineService.disable(projectId, id);
        return ResponseEntity.noContent().build();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "删除自动化部署流水线")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @PathVariable(value = "id") Long id) {
        pipelineService.delete(projectId, id);
        return ResponseEntity.noContent().build();
    }
}

