package io.choerodon.devops.api.controller.v1;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.CiCdPipelineRecordVO;
import io.choerodon.devops.app.service.CiCdPipelineRecordService;
import io.choerodon.swagger.annotation.Permission;

@RestController
@RequestMapping("/v1/projects/{project_id}/cicd_pipelines_record")
public class CiCdPipelineRecordController {

    @Autowired
    private CiCdPipelineRecordService ciCdPipelineRecordService;


    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询指定流水线记录详情")
    @GetMapping("/details")
    public ResponseEntity<CiCdPipelineRecordVO> queryPipelineRecordDetails(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "gitlab流水线Id", required = true)
            @RequestParam(value = "gitlab_pipeline_id") Long gitlabPipelineId,
            @ApiParam(value = "cd流水线记录id", required = true)
            @RequestParam(value = "cd_pipeline_record_id") Long pipelineRecordId) {
        return ResponseEntity.ok(ciCdPipelineRecordService.queryPipelineRecordDetails(projectId, gitlabPipelineId, pipelineRecordId));
    }
}
