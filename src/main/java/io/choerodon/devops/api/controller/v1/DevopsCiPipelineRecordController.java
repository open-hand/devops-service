package io.choerodon.devops.api.controller.v1;

import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.annotation.Permission;
import io.choerodon.core.enums.ResourceType;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.DevopsCiPipelineRecordVO;
import io.choerodon.devops.app.service.DevopsCiPipelineRecordService;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:31
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/ci_pipeline_records")
public class DevopsCiPipelineRecordController {
    private DevopsCiPipelineRecordService devopsCiPipelineRecordService;

    public DevopsCiPipelineRecordController(DevopsCiPipelineRecordService devopsCiPipelineRecordService) {
        this.devopsCiPipelineRecordService = devopsCiPipelineRecordService;
    }
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询ci流水线执行记录")
    @GetMapping("/{ci_pipeline_id}")
    public ResponseEntity<PageInfo<DevopsCiPipelineRecordVO>> pagingPipelineRecord(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "流水线Id", required = true)
            @PathVariable(value = "ci_pipeline_id") Long ciPipelineId,
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(devopsCiPipelineRecordService.pagingPipelineRecord(projectId, ciPipelineId, pageable));
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询指定流水线记录详情")
    @GetMapping("/{gitlab_pipeline_id}/details")
    public ResponseEntity<DevopsCiPipelineRecordVO> queryPipelineRecordDetails(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "gitlab流水线Id", required = true)
            @PathVariable(value = "gitlab_pipeline_id") Long gitlabPipelineId) {
        return ResponseEntity.ok(devopsCiPipelineRecordService.queryPipelineRecordDetails(projectId, gitlabPipelineId));
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "重试GitLab流水线")
    @PostMapping(value = "/{gitlab_pipeline_id}/retry")
    public ResponseEntity<Boolean> retry(
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
     *
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
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
