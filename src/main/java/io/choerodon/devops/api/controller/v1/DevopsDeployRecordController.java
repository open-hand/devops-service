package io.choerodon.devops.api.controller.v1;

import java.util.Date;
import java.util.Optional;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.DeployRecordCountVO;
import io.choerodon.devops.api.vo.DeployRecordVO;
import io.choerodon.devops.api.vo.DevopsDeployRecordVO;
import io.choerodon.devops.app.service.DevopsDeployRecordService;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * Created by Sheep on 2019/7/30.
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/deploy_record")
public class DevopsDeployRecordController {
    @Autowired
    private DevopsDeployRecordService devopsDeployRecordService;

    /**
     * 项目下获取部署记录
     *
     * @param projectId 项目Id
     * @param pageable  分页参数
     * @param params    查询参数
     * @return 部署纪录页
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下分页查询部署记录")
    @CustomPageRequest
    @PostMapping("/page_by_options")
    public ResponseEntity<Page<DevopsDeployRecordVO>> pageByOptions(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageable,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return Optional.ofNullable(devopsDeployRecordService.pageByProjectId(projectId, params, pageable))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.pipeline.value.list"));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "项目下分页查询部署记录")
    @CustomPageRequest
    @GetMapping("/paging")
    public ResponseEntity<Page<DeployRecordVO>> paging(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest,
            @ApiParam(value = "环境id")
            @Encrypt
            @RequestParam(value = "env_id", required = false) Long envId,
            @ApiParam(value = "应用服务id")
            @Encrypt
            @RequestParam(value = "app_service_id", required = false) Long appServiceId,
            @ApiParam(value = "部署类型")
            @RequestParam(value = "deploy_type", required = false) String deployType,
            @ApiParam(value = "部署结果")
            @RequestParam(value = "deploy_result", required = false) String deployResult
            ) {
        return ResponseEntity.ok(devopsDeployRecordService.paging(projectId, pageRequest, envId, appServiceId, deployType, deployResult));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, permissionWithin = true)
    @ApiOperation(value = "统计项目下指定时间段内每日部署次数")
    @CustomPageRequest
    @GetMapping("/count_by_date")
    public ResponseEntity<DeployRecordCountVO> countByDate(
            @PathVariable(value = "project_id") Long projectId,
            @RequestParam("startTime") Date startTime,
            @RequestParam("endTime") Date endTime) {
        return ResponseEntity.ok(devopsDeployRecordService.countByDate(projectId, startTime, endTime));
    }
}
