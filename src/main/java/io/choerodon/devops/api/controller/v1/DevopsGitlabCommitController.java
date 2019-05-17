package io.choerodon.devops.api.controller.v1;

import java.util.Date;
import java.util.Optional;

import io.choerodon.base.annotation.Permission;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.dto.CommitFormRecordDTO;
import io.choerodon.devops.api.dto.DevopsGitlabCommitDTO;
import io.choerodon.devops.app.service.DevopsGitlabCommitService;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Created by n!Ck
 * Date: 2018/9/19
 * Time: 15:06
 * Description:
 */

@RestController
@RequestMapping(value = "/v1/projects/{project_id}/commits")
public class DevopsGitlabCommitController {

    @Autowired
    private DevopsGitlabCommitService devopsGitlabCommitService;

    /**
     * 应用下commit记录报表
     *
     * @param projectId 项目id
     * @param appIds    应用id
     * @return DevopsGitlabCommitDTO
     */
    @Permission(
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取应用下的代码提交")
    @PostMapping
    public ResponseEntity<DevopsGitlabCommitDTO> getCommits(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用ids", required = true)
            @RequestBody String appIds,
            @ApiParam(value = "开始时间start_date", required = true)
            @RequestParam(value = "start_date") Date startDate,
            @ApiParam(value = "结束时间end_date", required = true)
            @RequestParam(value = "end_date") Date endDate) {
        return Optional.ofNullable(devopsGitlabCommitService.getCommits(projectId, appIds, startDate, endDate))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.commits.get"));
    }

    /**
     * 应用获取下最近的commit记录
     *
     * @param projectId   项目id
     * @param appIds      应用id
     * @param pageRequest 分页参数
     * @return List
     */
    @Permission(
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @CustomPageRequest
    @ApiOperation(value = "获取应用下的代码提交历史记录")
    @PostMapping("/record")
    public ResponseEntity<Page<CommitFormRecordDTO>> getRecordCommits(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用ids", required = true)
            @RequestBody String appIds,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "开始时间start_date", required = true)
            @RequestParam(value = "start_date") Date startDate,
            @ApiParam(value = "结束时间end_date", required = true)
            @RequestParam(value = "end_date") Date endDate) {
        return Optional.ofNullable(devopsGitlabCommitService.getRecordCommits(projectId, appIds, pageRequest,
                startDate, endDate))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.record.commit.get"));
    }
}
