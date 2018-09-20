package io.choerodon.devops.api.controller.v1;


import java.util.Date;
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
import io.choerodon.devops.api.dto.PipelineFrequencyDTO;
import io.choerodon.devops.api.dto.PipelineTimeDTO;
import io.choerodon.devops.app.service.DevopsGitlabPipelineService;
import io.choerodon.swagger.annotation.Permission;

@RestController
@RequestMapping(value = "/v1/projects/{project_id}/pipeline")
public class DevopsGitlabPipelineController {


    @Autowired
    private DevopsGitlabPipelineService devopsGitlabPipelineService;

    /**
     * 获取pipeline时长报表
     *
     * @param projectId 项目id
     * @param appId     应用id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return List
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER,
                    InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "获取pipeline时长报表")
    @GetMapping(value = "/time")
    public ResponseEntity<PipelineTimeDTO> listPipelineTime(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "appId")
            @RequestParam(required = false) Long appId,
            @ApiParam(value = "startTime")
            @RequestParam(required = true) Date startTime,
            @ApiParam(value = "endTime")
            @RequestParam(required = true) Date endTime) {
        return Optional.ofNullable(devopsGitlabPipelineService.getPipelineTime(appId, startTime, endTime))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.pipeline.time.get"));
    }


    /**
     * 获取pipeline次数报表
     *
     * @param projectId 项目id
     * @param appId     应用id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return List
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER,
                    InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "获取pipeline次数报表")
    @GetMapping(value = "/frequency")
    public ResponseEntity<PipelineFrequencyDTO> listPipelineFrequency(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "appId")
            @RequestParam(required = false) Long appId,
            @ApiParam(value = "startTime")
            @RequestParam(required = true) Date startTime,
            @ApiParam(value = "endTime")
            @RequestParam(required = true) Date endTime) {
        return Optional.ofNullable(devopsGitlabPipelineService.getPipelineFrequency(appId, startTime, endTime))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.pipeline.frequency.get"));
    }


}
