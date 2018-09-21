package io.choerodon.devops.api.controller.v1;

import java.util.Optional;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.dto.CommitFormRecordDTO;
import io.choerodon.devops.api.dto.DevopsGitlabCommitDTO;
import io.choerodon.devops.app.service.DevopsGitlabCommitService;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * Created by n!Ck
 * Date: 2018/9/19
 * Time: 15:06
 * Description:
 */

@RestController
@RequestMapping(value = "/v1/projects/{project_id}/apps")
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
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取应用下的commits")
    @PostMapping("/commits")
    public ResponseEntity<DevopsGitlabCommitDTO> getCommits(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用id", required = true)
            @RequestParam(value = "app_ids") String[] appIds) {
        return Optional.ofNullable(devopsGitlabCommitService.getCommits(appIds))
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
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @CustomPageRequest
    @ApiOperation(value = "获取应用下的commits")
    @PostMapping("/commits/record")
    public ResponseEntity<Page<CommitFormRecordDTO>> get(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用id", required = true)
            @RequestParam(value = "app_ids") String[] appIds,
            @ApiParam(value = "分页参数")
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest) {
        return Optional.ofNullable(devopsGitlabCommitService.getRecordCommits(appIds, pageRequest))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.record.commit.get"));
    }
}
