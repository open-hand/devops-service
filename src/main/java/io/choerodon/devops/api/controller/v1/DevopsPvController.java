package io.choerodon.devops.api.controller.v1;

import com.github.pagehelper.PageInfo;
import io.choerodon.core.annotation.Permission;
import org.springframework.data.domain.Pageable;
import io.choerodon.core.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.DevopsPvServcie;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/v1/projects/{project_id}/pv")
public class DevopsPvController {

    private static final String ERROR_PV_QUERY = "error.pv.query";

    @Autowired
    DevopsPvServcie devopsPvServcie;

    /**
     * 分页带参数查询项目下所有pv
     * @param projectId
     * @return
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "分页条件查询pv列表")
    @PostMapping("/page_by_options")
    public ResponseEntity<PageInfo<DevopsPvVO>> queryAll(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id")Long projectId,
            @ApiParam(value = "是否需要分页")
            @RequestParam(value = "doPage", required = false)
            Boolean doPage,
            @ApiParam(value = "分页参数")
            @ApiIgnore Pageable pageable,
            @ApiParam(value = "模糊搜索参数")
            @RequestBody(required = false) String params) {
        return Optional.ofNullable(devopsPvServcie.pageByOptions(doPage, pageable, params))
                .map(target -> new ResponseEntity(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(ERROR_PV_QUERY));
    }

    /**
     * 创建pv
     * @param projectId
     * @param devopsPvReqVo
     * @return
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "创建pv")
    @PostMapping
    public ResponseEntity createPv(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id")Long projectId,
            @RequestBody @Valid DevopsPvReqVO devopsPvReqVo){
        devopsPvServcie.createPv(devopsPvReqVo);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    /**
     * 校验pv的名称是否满足所选集群下唯一
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "校验pv的名称是否满足所选集群下唯一")
    @GetMapping("check_name")
    public ResponseEntity checkPvName(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id")Long projectId,
            @ApiParam(value = "集群Id", required = true)
            @RequestParam Long clusterId,
            @ApiParam(value = "pv名称" ,required = true)
            @RequestParam String pvName){
        devopsPvServcie.checkName(clusterId, pvName);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    /**
     * 根据pvId删除Pv
     * @param pvId
     * @return
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "根据pvId删除Pv")
    @DeleteMapping("/{pv_id}")
    public ResponseEntity deletePv(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id")Long projectId,
            @ApiParam(value = "pvId", required = true)
            @PathVariable(value = "pv_id") Long pvId){
        devopsPvServcie.deletePvById(pvId);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    /**
     * 根据pvId查询相应pv
     * @return
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "根据pvId查询相应pv")
    @GetMapping("/{pv_id}")
    public ResponseEntity<DevopsPvVO> queryById(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id")Long projectId,
            @ApiParam(value = "pvId", required = true)
            @PathVariable(value = "pv_id")Long pvId){
        return Optional.ofNullable(devopsPvServcie.queryById(pvId))
                .map(target -> new ResponseEntity(target, HttpStatus.OK))
                .orElseThrow(() ->new CommonException(ERROR_PV_QUERY));
    }

    /**
     * 列出组织下所有项目中在数据库中没有和当前Pv有权限关联关系的项目(不论当前数据库中是否跳过权限检查)
     *
     * @param projectId 项目ID
     * @param pvId    PVID
     * @param params    搜索参数
     * @return 所有与该证书未分配权限的项目
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "列出组织下所有项目中没有分配权限的项目")
    @PostMapping(value = "/{pv_id}/permission/list_non_related")
    public ResponseEntity<List<ProjectReqVO>> listAllNonRelatedMembers(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "PvId", required = true)
            @PathVariable(value = "pv_id") Long pvId,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return Optional.ofNullable(devopsPvServcie.listNonRelatedProjects(projectId, pvId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.get.pv.non.related.project"));
    }

    /**
     * 根据项目id删除与当前pv相关的权限记录
     * @param projectId
     * @param pvId
     * @param relatedProjectId
     * @return
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "根据projectId删除和Pv关联的权限记录")
    @DeleteMapping(value = "/{pv_id}/permission")
    public ResponseEntity deleteRelateProjectById(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "PvId", required = true)
            @PathVariable(value = "pv_id") Long pvId,
            @ApiParam(value = "要删除的proejctId")
            @RequestParam(value = "related_project_id") Long relatedProjectId){
        devopsPvServcie.deleteRelatedProjectById(pvId, projectId);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    /***
     * 给pv设置相对应的项目权限
     * @param projectId
     * @param pvId
     * @param devopsPvPermissionUpdateVO
     * @return
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "给当前pv分配项目权限")
    @PostMapping(value = "/{pv_id}/permission")
    public ResponseEntity assignPermission(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "PvId", required = true)
            @PathVariable(value = "pv_id") Long pvId,
            @RequestBody @Valid DevopsPvPermissionUpdateVO devopsPvPermissionUpdateVO){
        devopsPvServcie.assignPermission(devopsPvPermissionUpdateVO);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }


    /**
     * 分页查询PV下已有权限的项目列表
     * @param projectId
     * @param pvId
     * @param pageable
     * @param params
     * @return
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "分页查询PV下已有权限的项目列表")
    @PostMapping("/{pv_id}/permission/page_related")
    public ResponseEntity<PageInfo<ProjectReqVO>> pageRelatedProjects(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "PvId")
            @PathVariable(value = "pv_id") Long pvId,
            @ApiParam(value = "分页参数")
            @ApiIgnore Pageable pageable,
            @ApiParam(value = "模糊搜索参数")
            @RequestBody(required = false) String params) {
        return Optional.ofNullable(devopsPvServcie.pageRelatedProjects(projectId, pvId, pageable, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(ERROR_PV_QUERY));
    }

}
