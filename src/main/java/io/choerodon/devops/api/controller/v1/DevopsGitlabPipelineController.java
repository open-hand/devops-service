package io.choerodon.devops.api.controller.v1;


import java.util.Date;
import java.util.Optional;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.annotation.Permission;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.DevopsGitlabPipelineVO;
import io.choerodon.devops.api.vo.PipelineFrequencyVO;
import io.choerodon.devops.api.vo.PipelineTimeVO;
import io.choerodon.devops.app.service.DevopsGitlabPipelineService;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @Permission(type= ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取pipeline时长报表")
    @GetMapping(value = "/time")
    public ResponseEntity<PipelineTimeVO> listPipelineTime(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "app_id")
            @RequestParam(value = "app_id",required = false) Long appId,
            @ApiParam(value = "start_time")
            @RequestParam(value = "start_time") Date startTime,
            @ApiParam(value = "end_time")
            @RequestParam(value = "end_time") Date endTime) {
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
     * @return 次数报表
     */
    @Permission(type= ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取pipeline次数报表")
    @GetMapping(value = "/frequency")
    public ResponseEntity<PipelineFrequencyVO> listPipelineFrequency(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "app_id")
            @RequestParam(value = "app_id",required = false) Long appId,
            @ApiParam(value = "start_time")
            @RequestParam(value = "start_time") Date startTime,
            @ApiParam(value = "end_time")
            @RequestParam(value = "end_time") Date endTime) {
        return Optional.ofNullable(devopsGitlabPipelineService.getPipelineFrequency(appId, startTime, endTime))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.pipeline.frequency.get"));
    }


    /**
     * 分页获取pipeline
     *
     * @param projectId 项目id
     * @param appId     应用id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return List
     */
    @Permission(type= ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "分页获取pipeline")
    @CustomPageRequest
    @GetMapping(value = "/page_by_options")
    public ResponseEntity<PageInfo<DevopsGitlabPipelineVO>> pageByOptions(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
                    PageRequest pageRequest,
            @ApiParam(value = "branch")
            @RequestParam(required = false) String branch,
            @ApiParam(value = "app_id")
            @RequestParam(value = "app_id",required = false) Long appId,
            @ApiParam(value = "start_time")
            @RequestParam(value = "start_time") Date startTime,
            @ApiParam(value = "end_time")
            @RequestParam(value = "end_time") Date endTime) {
        return Optional.ofNullable(devopsGitlabPipelineService.pageByOptions(appId, branch, pageRequest, startTime, endTime))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.pipeline.frequency.get"));
    }
}
