package io.choerodon.devops.api.controller.v1;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.dto.PipelineAppDeployDTO;
import io.choerodon.devops.app.service.PipelineTaskService;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:22 2019/4/4
 * Description:
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/pipe_task")
public class PipelineTaskController {

    @Autowired
    private PipelineTaskService pipelineTaskService;

    /**
     * 项目下创建应用
     *
     * @param projectId    项目id
     * @param appDeployDTO 部署任务信息
     * @return PipelineAppDeployDTO
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下创建应用")
    @PostMapping
    public ResponseEntity<PipelineAppDeployDTO> create(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用信息", required = true)
            @RequestBody PipelineAppDeployDTO appDeployDTO) {
        return Optional.ofNullable(pipelineTaskService.createAppDeploy(projectId, appDeployDTO))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.pipeline.app.deploy.create"));
    }

}
