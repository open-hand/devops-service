package io.choerodon.devops.api.controller.v1;


import java.util.Date;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.DevopsGitlabPipelineVO;
import io.choerodon.devops.api.vo.PipelineFrequencyVO;
import io.choerodon.devops.api.vo.PipelineTimeVO;
import io.choerodon.devops.app.service.DevopsGitlabPipelineService;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

@Api(hidden = true)
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/pipeline")
public class DevopsGitlabPipelineController {


    @Autowired
    private DevopsGitlabPipelineService devopsGitlabPipelineService;

    /**
     * 获取pipeline时长报表
     *
     * @param projectId    项目id
     * @param appServiceId 服务id
     * @param startTime    开始时间
     * @param endTime      结束时间
     * @return List
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取pipeline时长报表")
    @GetMapping(value = "/time")
    public ResponseEntity<PipelineTimeVO> listPipelineTime(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "app_service_id")
            @RequestParam(value = "app_service_id", required = false) Long appServiceId,
            @ApiParam(value = "start_time")
            @RequestParam(value = "start_time") Date startTime,
            @ApiParam(value = "end_time")
            @RequestParam(value = "end_time") Date endTime) {
        return ResponseEntity.ok(devopsGitlabPipelineService.getPipelineTime(appServiceId, startTime, endTime));
    }


    /**
     * 获取pipeline次数报表
     *
     * @param projectId    项目id
     * @param appServiceId 服务id
     * @param startTime    开始时间
     * @param endTime      结束时间
     * @return 次数报表
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取pipeline次数报表")
    @GetMapping(value = "/frequency")
    public ResponseEntity<PipelineFrequencyVO> listPipelineFrequency(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "app_service_id")
            @RequestParam(value = "app_service_id", required = false) Long appServiceId,
            @ApiParam(value = "start_time")
            @RequestParam(value = "start_time") Date startTime,
            @ApiParam(value = "end_time")
            @RequestParam(value = "end_time") Date endTime) {
        return ResponseEntity.ok(devopsGitlabPipelineService.getPipelineFrequency(appServiceId, startTime, endTime));
    }


    /**
     * 分页获取pipeline
     *
     * @param projectId    项目id
     * @param appServiceId 服务id
     * @param startTime    开始时间
     * @param endTime      结束时间
     * @return List
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "分页获取pipeline")
    @CustomPageRequest
    @GetMapping(value = "/page_by_options")
    public ResponseEntity<Page<DevopsGitlabPipelineVO>> pageByOptions(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
                    PageRequest pageable,
            @ApiParam(value = "branch")
            @RequestParam(required = false) String branch,
            @Encrypt
            @ApiParam(value = "app_service_id")
            @RequestParam(value = "app_service_id", required = false) Long appServiceId,
            @ApiParam(value = "start_time")
            @RequestParam(required = false, value = "start_time") Date startTime,
            @ApiParam(value = "end_time")
            @RequestParam(required = false, value = "end_time") Date endTime) {
        return Results.success(devopsGitlabPipelineService.pageByOptions(appServiceId, branch, pageable, startTime, endTime));
    }
}
