package io.choerodon.devops.api.controller.v1;

import java.util.Objects;
import javax.validation.Valid;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.ProjectCertificationCreateUpdateVO;
import io.choerodon.devops.api.vo.ProjectCertificationPermissionUpdateVO;
import io.choerodon.devops.api.vo.ProjectCertificationVO;
import io.choerodon.devops.api.vo.ProjectReqVO;
import io.choerodon.devops.app.service.DevopsProjectCertificationService;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * 泛域名证书
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/certs")
public class ProjectCertificationController {

    @Autowired
    private DevopsProjectCertificationService devopsProjectCertificationService;

    /**
     * 项目下创建或更新证书
     *
     * @param projectId              项目Id
     * @param projectCertificationVO 证书信息
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下创建或更新证书")
    @PostMapping
    public ResponseEntity<Void> createOrUpdate(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "证书信息", required = true)
            // TODO 等hzero主键加密兼容 ModelAttribute
            @ModelAttribute @Valid ProjectCertificationCreateUpdateVO projectCertificationVO,
            BindingResult bindingResult,
            @ApiParam(value = "key文件")
            @RequestParam(value = "key", required = false) MultipartFile key,
            @ApiParam(value = "cert文件")
            @RequestParam(value = "cert", required = false) MultipartFile cert) {
        if (bindingResult.hasErrors()) {
            throw new CommonException(Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }
        devopsProjectCertificationService.createOrUpdate(projectId, key, cert, projectCertificationVO);
        return ResponseEntity.ok().build();
    }

    /**
     * 查询单个证书信息
     *
     * @param projectId 项目Id
     * @param certId    证书Id
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询单个证书信息")
    @GetMapping("/{cert_id}")
    public ResponseEntity<ProjectCertificationVO> query(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "证书Id")
            @PathVariable(value = "cert_id") Long certId) {
        return ResponseEntity.ok(devopsProjectCertificationService.queryCert(certId));
    }

    /**
     * 校验证书名唯一性
     *
     * @param projectId 项目id
     * @param name      证书name
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "校验证书名唯一性")
    @GetMapping(value = "/check_name")
    public ResponseEntity<Boolean> checkName(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "证书name", required = true)
            @RequestParam String name) {
        return ResponseEntity.ok(devopsProjectCertificationService.isNameUnique(projectId, name));
    }


    /**
     * 分页查询证书下已有权限的项目列表
     *
     * @param projectId 项目id
     * @param certId    证书id
     * @param pageable  分页参数
     * @param params    查询参数
     * @return page
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "分页查询证书下已有权限的项目列表")
    @PostMapping("/{cert_id}/permission/page_related")
    public ResponseEntity<Page<ProjectReqVO>> pageRelatedProjects(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "证书Id")
            @PathVariable(value = "cert_id") Long certId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageable,
            @ApiParam(value = "模糊搜索参数")
            @RequestBody(required = false) String params) {
        return ResponseEntity.ok(devopsProjectCertificationService.pageRelatedProjects(projectId, certId, pageable, params));
    }


    /**
     * 列出组织下所有项目中在数据库中没有权限关联关系的项目(不论当前数据库中是否跳过权限检查)
     *
     * @param projectId 项目ID
     * @param certId    证书ID
     * @param params    搜索参数
     * @return 所有与该证书未分配权限的项目
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "列出组织下所有项目中没有分配权限的项目")
    @PostMapping(value = "/{cert_id}/permission/list_non_related")
    public ResponseEntity<Page<ProjectReqVO>> listAllNonRelatedMembers(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "证书id", required = true)
            @PathVariable(value = "cert_id") Long certId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageable,
            @ApiParam(value = "指定项目Id")
            @RequestParam(value = "id", required = false) Long selectedProjectId,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return ResponseEntity.ok(devopsProjectCertificationService.listNonRelatedMembers(projectId, certId, selectedProjectId, pageable, params));
    }


    /**
     * 删除项目在该证书下的权限
     *
     * @param projectId 项目id
     * @param certId    证书id
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "删除项目在该证书下的权限")
    @DeleteMapping(value = "/{cert_id}/permission")
    public ResponseEntity<Void> deletePermissionOfProject(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "证书id", required = true)
            @PathVariable(value = "cert_id") Long certId,
            @ApiParam(value = "关联的项目ID", required = true)
            @RequestParam(value = "related_project_id") Long relatedProjectId) {
        devopsProjectCertificationService.deletePermissionOfProject(relatedProjectId, certId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 项目证书列表查询
     *
     * @param projectId 项目ID
     * @return Page
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目证书列表查询")
    @CustomPageRequest
    @PostMapping("/page_cert")
    public ResponseEntity<Page<ProjectCertificationVO>> pageOrgCert(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageable,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return ResponseEntity.ok(devopsProjectCertificationService.pageCerts(projectId, pageable, params));
    }

    /**
     * 证书下为项目分配权限
     *
     * @param certId             证书id
     * @param permissionUpdateVO 权限分配信息
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "证书下为项目分配权限")
    @PostMapping(value = "/{cert_id}/permission")
    public ResponseEntity<Void> assignPermission(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "证书id", required = true)
            @PathVariable(value = "cert_id") Long certId,
            @ApiParam(value = "权限分配信息")
            @RequestBody @Valid ProjectCertificationPermissionUpdateVO permissionUpdateVO) {
        devopsProjectCertificationService.assignPermission(permissionUpdateVO);
        return ResponseEntity.ok().build();
    }

    /**
     * 删除证书
     *
     * @param projectId 项目ID
     * @param certId    证书Id
     * @return String
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "删除证书")
    @CustomPageRequest
    @DeleteMapping("/{cert_id}")
    public ResponseEntity<Void> deleteOrgCert(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "证书Id")
            @PathVariable(value = "cert_id") Long certId) {
        devopsProjectCertificationService.deleteCert(projectId, certId);
        return ResponseEntity.ok().build();
    }
}
