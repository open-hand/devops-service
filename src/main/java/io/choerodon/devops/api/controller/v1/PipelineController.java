package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.base.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.PipelineHomeVO;
import io.choerodon.devops.api.vo.PipelineVO;
import io.choerodon.devops.api.vo.SearchVO;
import io.choerodon.devops.app.service.PipelinePersonalTokenService;
import io.choerodon.devops.app.service.PipelineService;
import io.choerodon.devops.infra.dto.PipelineDTO;
import io.choerodon.devops.infra.dto.PipelineRecordDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
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
    @Autowired
    private PipelinePersonalTokenService pipelinePersonalTokenService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "校验名称项目下是否存在")
    @GetMapping("/check_name")
    public ResponseEntity<Boolean> checkName(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "修改时需要传，流水线id", required = true)
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam(value = "name") String name) {
        return ResponseEntity.ok(pipelineService.checkName(projectId, id, name));
    }

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
    @ApiOperation(value = "项目下更新自动化部署流水线")
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @PathVariable(value = "id") Long id,
            @RequestBody @Valid PipelineVO pipelineVO) {
        pipelineService.update(projectId, id, pipelineVO);
        return ResponseEntity.noContent().build();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询自动化部署流水线详情")
    @GetMapping("/{id}")
    public ResponseEntity<PipelineVO> query(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @PathVariable(value = "id") Long id) {
        return ResponseEntity.ok(pipelineService.query(projectId, id));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "分页查询自动化部署流水线")
    @PostMapping("/paging")
    @CustomPageRequest
    public ResponseEntity<Page<PipelineHomeVO>> paging(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @RequestBody SearchVO searchVO) {
        return ResponseEntity.ok(pipelineService.paging(projectId, pageRequest, searchVO));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "启用自动化部署流水线")
    @PutMapping("/{id}/enable")
    public ResponseEntity<Void> enable(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
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
            @PathVariable(value = "id") Long id) {
        pipelineService.delete(projectId, id);
        return ResponseEntity.noContent().build();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "执行自动化部署流水线")
    @PostMapping("/{id}/execute")
    public ResponseEntity<PipelineRecordDTO> execute(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @PathVariable(value = "id") Long id) {
        return ResponseEntity.ok(pipelineService.executeByManual(projectId, id));
    }

    @Permission(permissionPublic = true)
    @ApiOperation(value = "通过令牌执行自动化部署流水线")
    @PostMapping("/execute_by_token")
    public ResponseEntity<PipelineRecordDTO> executeByToken(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @RequestParam(value = "token") String token,
            @RequestParam(value = "personal_token") String personalToken) {
        return ResponseEntity.ok(pipelineService.executeByToken(projectId, token, personalToken));
    }

    @Permission(permissionLogin = true)
    @ApiOperation(value = "查询个人令牌")
    @GetMapping("/personal_token")
    public ResponseEntity<String> queryOrCreatePersonalToken(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return ResponseEntity.ok(pipelinePersonalTokenService.queryOrCreatePersonalToken(projectId));
    }

    @Permission(permissionLogin = true)
    @ApiOperation(value = "重置个人令牌")
    @PutMapping("/personal_token/reset")
    public ResponseEntity<String> resetPersonalToken(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return ResponseEntity.ok(pipelinePersonalTokenService.resetPersonalToken(projectId));
    }
}

