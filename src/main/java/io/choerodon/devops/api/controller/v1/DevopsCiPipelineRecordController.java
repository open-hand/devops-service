package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.DevopsCiPipelineRecordVO;
import io.choerodon.devops.app.service.DevopsCiPipelineRecordService;
import io.choerodon.devops.infra.dto.DevopsCiPipelineRecordDTO;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.Permission;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/4/3 9:31
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/ci_pipeline_records")
public class DevopsCiPipelineRecordController {
    private final DevopsCiPipelineRecordService devopsCiPipelineRecordService;

    public DevopsCiPipelineRecordController(DevopsCiPipelineRecordService devopsCiPipelineRecordService) {
        this.devopsCiPipelineRecordService = devopsCiPipelineRecordService;
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询ci流水线执行记录")
    @GetMapping("/{ci_pipeline_id}")
    public ResponseEntity<Page<DevopsCiPipelineRecordVO>> pagingPipelineRecord(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "流水线Id", required = true)
            @PathVariable(value = "ci_pipeline_id") Long ciPipelineId,
            @ApiIgnore
            @SortDefault(value = DevopsCiPipelineRecordDTO.FIELD_GITLAB_PIPELINE_ID, direction = Sort.Direction.DESC) PageRequest pageable) {
        return ResponseEntity.ok(devopsCiPipelineRecordService.pagingPipelineRecord(projectId, ciPipelineId, pageable));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询指定流水线记录详情")
    @GetMapping("/{gitlab_pipeline_id}/details")
    public ResponseEntity<DevopsCiPipelineRecordVO> queryPipelineRecordDetails(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "gitlab流水线Id", required = true)
            @PathVariable(value = "gitlab_pipeline_id") Long gitlabPipelineId) {
        return ResponseEntity.ok(devopsCiPipelineRecordService.queryPipelineRecordDetails(projectId, gitlabPipelineId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "重试GitLab流水线")
    @PostMapping(value = "/{gitlab_pipeline_id}/retry")
    public ResponseEntity<Boolean> retry(
            @Encrypt
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "gitlab项目ID", required = true)
            @PathVariable("gitlab_pipeline_id") Long gitlabPipelineId,
            @ApiParam(value = "流水线ID", required = true)
            @RequestParam("gitlab_project_id") Long gitlabProjectId) {
        devopsCiPipelineRecordService.retry(projectId, gitlabPipelineId, gitlabProjectId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Cancel jobs in a pipeline
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "取消GitLab流水线")
    @PostMapping(value = "/{gitlab_pipeline_id}/cancel")
    public ResponseEntity<Boolean> cancel(
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "gitlab项目ID", required = true)
            @PathVariable("gitlab_pipeline_id") Long gitlabPipelineId,
            @ApiParam(value = "流水线ID", required = true)
            @RequestParam("gitlab_project_id") Long gitlabProjectId) {
        devopsCiPipelineRecordService.cancel(projectId, gitlabPipelineId, gitlabProjectId);
        return ResponseEntity.noContent().build();
    }
}
