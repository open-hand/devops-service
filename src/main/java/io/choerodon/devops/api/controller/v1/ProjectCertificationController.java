package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Optional;

import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.ProjectCertificationVO;
import io.choerodon.devops.api.vo.ProjectReqVO;
import io.choerodon.devops.app.service.DevopsProjectCertificationService;
import io.choerodon.swagger.annotation.CustomPageRequest;

/**
 * 泛域名证书
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/certs")
public class ProjectCertificationController {

    @Autowired
    private DevopsProjectCertificationService devopsProjectCertificationService;

    /**
     * 项目下创建证书
     *
     * @param projectCertificationId 项目Id
     * @param projectCertificationVO 证书信息
     */
    @Permission(type = ResourceType.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "项目下创建证书")
    @PostMapping
    public ResponseEntity create(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectCertificationId,
            @ApiParam(value = "证书信息", required = true)
            @ModelAttribute ProjectCertificationVO projectCertificationVO,
            @ApiParam(value = "key文件")
            @RequestParam(value = "key", required = false) MultipartFile key,
            @ApiParam(value = "cert文件")
            @RequestParam(value = "cert", required = false) MultipartFile cert) {
        devopsProjectCertificationService.create(projectCertificationId, key, cert, projectCertificationVO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 更新证书下的项目
     *
     * @param project_id             项目Id
     * @param projectCertificationVO 证书项目关联对象
     */
    @Permission(type = ResourceType.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "更新证书下的项目")
    @PutMapping()
    public ResponseEntity update(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long project_id,
            @ApiParam(value = "证书ID")
            @PathVariable Long certId,
            @ApiParam(value = "证书组织关联对象")
            @RequestBody ProjectCertificationVO projectCertificationVO) {
        devopsProjectCertificationService.update(certId, projectCertificationVO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 查询单个证书信息
     *
     * @param projectCertificationId 项目Id
     * @param certId                 证书Id
     */
    @Permission(type = ResourceType.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "查询单个证书信息")
    @GetMapping("/{cert_id}")
    public ResponseEntity<ProjectCertificationVO> query(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectCertificationId,
            @ApiParam(value = "证书Id")
            @PathVariable(value = "cert_id") Long certId) {
        return Optional.ofNullable(devopsProjectCertificationService.queryCert(certId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.cert.query"));
    }

    /**
     * 校验证书名唯一性
     *
     * @param projectCertificationId 项目id
     * @param name                   证书name
     */
    @Permission(type = ResourceType.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "校验证书名唯一性")
    @GetMapping(value = "/check_name")
    public void checkName(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectCertificationId,
            @ApiParam(value = "证书name", required = true)
            @RequestParam String name) {
        devopsProjectCertificationService.checkName(projectCertificationId, name);
    }

    /**
     * 分页查询项目列表
     *
     * @param projectCertificationId 项目id
     * @return Page
     */
    @Permission(type = ResourceType.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "分页查询项目列表")
    @CustomPageRequest
    @PostMapping("/page_projects")
    public ResponseEntity<PageInfo<ProjectReqVO>> pageProjects(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectCertificationId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "证书Id")
            @RequestParam(required = false) Long certId,
            @ApiParam(value = "模糊搜索参数")
            @RequestBody String[] params) {
        return Optional.ofNullable(devopsProjectCertificationService.pageProjects(projectCertificationId, certId, pageRequest, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.project.query"));
    }

    /**
     * 查询已有权限的项目列表
     *
     * @param projectCertificationId 项目id
     * @return List
     */
    @Permission(type = ResourceType.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "查询已有权限的项目列表")
    @GetMapping("/list_cert_projects/{cert_id}")
    public ResponseEntity<List<ProjectReqVO>> listCertProjects(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectCertificationId,
            @ApiParam(value = "证书Id")
            @PathVariable(value = "cert_id") Long certId) {
        return Optional.ofNullable(devopsProjectCertificationService.listCertProjects(certId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.project.query"));
    }


    /**
     * 项目证书列表查询
     *
     * @param projectCertificationId 项目ID
     * @return Page
     */
    @Permission(type = ResourceType.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "项目证书列表查询")
    @CustomPageRequest
    @PostMapping("/page_cert")
    public ResponseEntity<PageInfo<ProjectCertificationVO>> pageOrgCert(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectCertificationId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody String params) {
        return Optional.ofNullable(devopsProjectCertificationService.pageCerts(projectCertificationId, pageRequest, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.cert.query"));
    }

    /**
     * 删除证书
     *
     * @param projectCertificationId 项目ID
     * @param certId                 证书Id
     * @return String
     */
    @Permission(type = ResourceType.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "删除证书")
    @CustomPageRequest
    @DeleteMapping("/{cert_id}")
    public ResponseEntity<String> deleteOrgCert(
            @ApiParam(value = "组织ID", required = true)
            @PathVariable(value = "project_id") Long projectCertificationId,
            @ApiParam(value = "证书Id")
            @PathVariable(value = "cert_id") Long certId) {
        devopsProjectCertificationService.deleteCert(certId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
