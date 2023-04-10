package io.choerodon.devops.api.controller.v1;

import javax.validation.Valid;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.DevopsPvcReqVO;
import io.choerodon.devops.api.vo.DevopsPvcRespVO;
import io.choerodon.devops.app.service.DevopsPvcService;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

@RestController
@RequestMapping(value = "/v1/projects/{project_id}/pvcs")
public class DevopsPvcController {

    @Autowired
    DevopsPvcService devopsPvcService;

    /**
     * 分页查询
     *
     * @param projectId 项目id
     * @param envId     环境ID
     * @param pageable  分页参数
     * @param params    查询参数
     * @return CertificationDTO page
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "分页查询PVC")
    @CustomPageRequest
    @PostMapping("/page_by_options")
    public ResponseEntity<Page<DevopsPvcRespVO>> pageByOptions(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境ID")
            @RequestParam(value = "env_id", required = false) Long envId,
            @ApiParam(value = "分页参数")
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageable,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return ResponseEntity.ok(devopsPvcService.pageByOptions(projectId, envId, pageable, params));
    }

    @PostMapping
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "创建PVC")
    public ResponseEntity<DevopsPvcRespVO> create(
            @ApiParam(value = "项目id", required = true)
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "PVC信息", required = true)
            @RequestBody @Valid DevopsPvcReqVO devopsPvcReqVO) {
        return ResponseEntity.ok(devopsPvcService.create(projectId, devopsPvcReqVO));
    }

    /**
     * 删除PVC
     *
     * @param envId 环境id
     * @param pvcId PVC id
     * @return Boolean
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "删除PVC")
    @DeleteMapping("/{pvc_id}")
    public ResponseEntity<Boolean> deletePvc(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境id", required = true)
            @RequestParam(value = "env_id") Long envId,
            @Encrypt
            @ApiParam(value = "PVC id", required = true)
            @PathVariable(value = "pvc_id") Long pvcId) {
        return ResponseEntity.ok(devopsPvcService.delete(projectId, envId, pvcId));
    }

    /**
     * 检查PVC名称的唯一性
     *
     * @param name  PVC名称
     * @param envId 环境id
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "检查PVC名称的唯一性")
    @GetMapping("/check_name")
    public ResponseEntity<Boolean> checkName(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境id", required = true)
            @RequestParam(value = "env_id") Long envId,
            @ApiParam(value = "PVC名称", required = true)
            @RequestParam(value = "name") String name) {
        return ResponseEntity.ok(devopsPvcService.isNameUnique(name, envId));
    }
}
