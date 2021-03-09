package io.choerodon.devops.api.controller.v1;

import java.util.Optional;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.app.service.ProjectPipelineService;
import io.choerodon.swagger.annotation.Permission;


/**
 * Created by Zenger on 2018/4/2.
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}")
public class ProjectPipelineController {

    @Autowired
    private ProjectPipelineService projectPipelineService;


    /**
     * Retry jobs in a pipeline
     *
     * @param projectId       项目id
     * @param gitlabProjectId gitlab项目id
     * @param pipelineId      流水线id
     * @return Boolean
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "重试GitLab流水线")
    @PostMapping(value = "/gitlab_projects/{gitlab_project_id}/pipelines/{pipeline_id}/retry")
    public ResponseEntity<Boolean> retry(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "gitlab项目ID", required = true)
            @PathVariable("gitlab_project_id") Long gitlabProjectId,
            @ApiParam(value = "流水线ID", required = true)
            @PathVariable("pipeline_id") Long pipelineId) {
        return Optional.ofNullable(projectPipelineService.retry(gitlabProjectId, pipelineId))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.pipeline.retry"));
    }

    /**
     * Cancel jobs in a pipeline
     *
     * @param projectId       项目id
     * @param gitlabProjectId gitlab项目id
     * @param pipelineId      流水线id
     * @return Boolean
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "取消GitLab流水线")
    @PostMapping(value = "/gitlab_projects/{gitlabProjectId}/pipelines/{pipeline_id}/cancel")
    public ResponseEntity<Boolean> cancel(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "gitlab项目ID", required = true)
            @PathVariable Long gitlabProjectId,
            @ApiParam(value = "流水线ID", required = true)
            @PathVariable(value = "pipeline_id") Long pipelineId) {
        return Optional.ofNullable(projectPipelineService.cancel(gitlabProjectId, pipelineId))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.pipeline.cancel"));
    }

    /**
     * Create a new pipeline
     *
     * @param projectId       项目id
     * @param gitlabProjectId gitlab项目id
     * @param ref             分支
     * @return Boolean
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "创建GitLab流水线")
    @PostMapping(value = "/gitlab_projects/{gitlab_project_id}/pipelines")
    public ResponseEntity<Boolean> create(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "gitlab项目ID", required = true)
            @PathVariable(value = "gitlab_project_id") Long gitlabProjectId,
            @ApiParam(value = "分支名", required = true)
            @RequestParam(value = "ref") String ref) {
        return Optional.ofNullable(projectPipelineService.create(gitlabProjectId, ref))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.pipeline.create"));
    }


}
