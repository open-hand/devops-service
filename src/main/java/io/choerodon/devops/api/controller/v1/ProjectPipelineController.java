package io.choerodon.devops.api.controller.v1;

import java.util.Optional;

import io.choerodon.base.annotation.Permission;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.app.service.ProjectPipelineService;
import io.choerodon.devops.domain.application.repository.GitlabProjectRepository;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Created by Zenger on 2018/4/2.
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}")
public class ProjectPipelineController {

    @Autowired
    GitlabProjectRepository gitlabRepository;
    private ProjectPipelineService projectPipelineService;

    public ProjectPipelineController(ProjectPipelineService projectPipelineService) {
        this.projectPipelineService = projectPipelineService;
    }

    /**
     * Retry jobs in a pipeline
     *
     * @param projectId       项目id
     * @param gitlabProjectId gitlab项目id
     * @param pipelineId      流水线id
     * @return Boolean
     */
    @Permission(roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "Retry jobs in a pipeline")
    @PostMapping(value = "/gitlab_projects/{gitlabProjectId}/pipelines/{pipelineId}/retry")
    public ResponseEntity<Boolean> retry(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "gitlab项目ID", required = true)
            @PathVariable Long gitlabProjectId,
            @ApiParam(value = "流水线ID", required = true)
            @PathVariable Long pipelineId) {
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
    @Permission(roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "Cancel jobs in a pipeline")
    @PostMapping(value = "/gitlab_projects/{gitlabProjectId}/pipelines/{pipelineId}/cancel")
    public ResponseEntity<Boolean> cancel(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "gitlab项目ID", required = true)
            @PathVariable Long gitlabProjectId,
            @ApiParam(value = "流水线ID", required = true)
            @PathVariable Long pipelineId) {
        return Optional.ofNullable(projectPipelineService.cancel(gitlabProjectId, pipelineId))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.pipeline.retry"));
    }
}
