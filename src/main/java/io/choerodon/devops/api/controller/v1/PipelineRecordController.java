package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.base.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.cd.PipelineRecordVO;
import io.choerodon.devops.app.service.PipelineRecordService;
import io.choerodon.devops.infra.dto.PipelineRecordDTO;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * 流水线执行记录(PipelineRecord)表控制层
 *
 * @author
 * @since 2022-11-23 16:43:02
 */

@RestController("pipelineRecordController.v1")
@RequestMapping("/v1/projects/{project_id}/pipeline_records")
public class PipelineRecordController extends BaseController {

    @Autowired
    private PipelineRecordService pipelineRecordService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "分页查询流水线执行记录")
    @GetMapping("/paging")
    @CustomPageRequest
    public ResponseEntity<Page<PipelineRecordVO>> paging(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "流水线Id", required = true)
            @RequestParam(value = "pipeline_id") Long pipelineId,
            @ApiIgnore
            @SortDefault(value = PipelineRecordDTO.FIELD_ID, direction = Sort.Direction.DESC) PageRequest pageable,
            @RequestParam(value = "audit_flag", defaultValue = "false") Boolean auditFlag) {
        return ResponseEntity.ok(pipelineRecordService.paging(projectId, pipelineId, auditFlag, pageable));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询流水线执行记录详情")
    @GetMapping("/{id}")
    public ResponseEntity<PipelineRecordVO> query(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "流水线记录Id", required = true)
            @PathVariable(value = "id") Long id) {
        return ResponseEntity.ok(pipelineRecordService.query(projectId, id));
    }


    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "取消流水线")
    @PutMapping(value = "/{id}/cancel")
    public ResponseEntity<Void> cancel(
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "流水线记录id", required = true)
            @PathVariable(value = "id") Long id) {
        pipelineRecordService.cancel(projectId, id);
        return ResponseEntity.noContent().build();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "重试流水线")
    @PutMapping(value = "/{id}/retry")
    public ResponseEntity<Void> retry(
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "流水线记录id", required = true)
            @PathVariable(value = "id") Long id) {
        pipelineRecordService.retry(projectId, id);
        return ResponseEntity.noContent().build();
    }
}

