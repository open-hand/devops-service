package io.choerodon.devops.api.controller.v1;

import java.util.Date;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.DeployRecordCountVO;
import io.choerodon.devops.api.vo.DeployRecordVO;
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
            @ApiParam(value = "部署类型")
            @RequestParam(value = "deploy_type", required = false) String deployType,
            @ApiParam(value = "部署方式")
            @RequestParam(value = "deploy_mode", required = false) String deployMode,
            @ApiParam(value = "部署载体名")
            @RequestParam(value = "deploy_payload_name", required = false) String deployPayloadName,
            @ApiParam(value = "部署结果")
            @RequestParam(value = "deploy_result", required = false) String deployResult,
            @ApiParam(value = "部署对象名")
            @RequestParam(value = "deploy_object_name", required = false) String deployObjectName,
            @ApiParam(value = "部署对象版本")
            @RequestParam(value = "deploy_object_version", required = false) String deployObjectVersion
    ) {
        return ResponseEntity.ok(devopsDeployRecordService.paging(projectId, pageRequest, deployType, deployMode, deployPayloadName, deployResult, deployObjectName, deployObjectVersion));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, permissionWithin = true)
    @ApiOperation(value = "统计项目下指定时间段内每日部署次数")
    @CustomPageRequest
    @GetMapping("/count_by_date")
    public ResponseEntity<DeployRecordCountVO> countByDate(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "开始时间", required = true)
            @RequestParam("startTime") Date startTime,
            @ApiParam(value = "结束时间", required = true)
            @RequestParam("endTime") Date endTime) {
        return ResponseEntity.ok(devopsDeployRecordService.countByDate(projectId, startTime, endTime));
    }

//    @Permission(level = ResourceLevel.ORGANIZATION)
//    @ApiOperation(value = "停止hzero部署", hidden = true)
//    @PutMapping("/{record_id}/stop")
//    public ResponseEntity<Void> stop(
//            @PathVariable(value = "project_id") Long projectId,
//            @Encrypt @PathVariable(value = "record_id") Long recordId) {
//        devopsDeployRecordService.stop(projectId, recordId);
//        return ResponseEntity.noContent().build();
//    }
//
//    @Permission(level = ResourceLevel.ORGANIZATION)
//    @ApiOperation(value = "重试hzero部署", hidden = true)
//    @PostMapping("/{record_id}/retry")
//    public ResponseEntity<Void> retry(
//            @PathVariable(value = "project_id") Long projectId,
//            @Encrypt @PathVariable(value = "record_id") Long recordId,
//            @RequestBody @Validated HzeroDeployVO hzeroDeployVO) {
//        devopsDeployRecordService.retry(projectId, recordId, hzeroDeployVO);
//        return ResponseEntity.noContent().build();
//    }
//
//    @Permission(level = ResourceLevel.ORGANIZATION)
//    @ApiOperation(value = "查询部署记录", hidden = true)
//    @GetMapping("/{record_id}")
//    public ResponseEntity<HzeroDeployRecordVO> queryHzeroDetailsById(
//            @PathVariable(value = "project_id") Long projectId,
//            @Encrypt @PathVariable(value = "record_id") Long recordId) {
//        return ResponseEntity.ok(devopsDeployRecordService.queryHzeroDetailsById(projectId, recordId));
//    }
}
