package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Optional;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.annotation.Permission;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.OrgCertificationDTO;
import io.choerodon.devops.api.vo.ProjectReqVO;
import io.choerodon.devops.app.service.DevopsOrgCertificationService;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value = "/v1/organizations/{organization_id}/certs")
public class OrgCertificationController {

    @Autowired
    private DevopsOrgCertificationService devopsOrgCertificationService;

    /**
     * 组织下创建证书
     *
     * @param organizationId      组织Id
     * @param orgCertificationDTO 证书信息
     */
    @Permission(type= ResourceType.ORGANIZATION,roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "组织下创建证书")
    @PostMapping
    public ResponseEntity create(
            @ApiParam(value = "组织Id", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "证书信息", required = true)
            @ModelAttribute OrgCertificationDTO orgCertificationDTO,
            @ApiParam(value = "key文件")
            @RequestParam(value = "key", required = false) MultipartFile key,
            @ApiParam(value = "cert文件")
            @RequestParam(value = "cert", required = false) MultipartFile cert) {
        devopsOrgCertificationService.insert(organizationId, key, cert, orgCertificationDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 更新证书下的项目
     *
     * @param organizationId      组织Id
     * @param orgCertificationDTO 集群对象
     */
    @Permission(type= ResourceType.ORGANIZATION,roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "更新证书下的项目")
    @PutMapping("/{certId}")
    public ResponseEntity update(
            @ApiParam(value = "组织Id", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "集群Id")
            @PathVariable Long certId,
            @ApiParam(value = "集群对象")
            @RequestBody OrgCertificationDTO orgCertificationDTO) {
        devopsOrgCertificationService.update(certId, orgCertificationDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 查询单个证书信息
     *
     * @param organizationId 组织Id
     * @param certId         集群Id
     */
    @Permission(type= ResourceType.ORGANIZATION,roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "查询单个证书信息")
    @GetMapping("/{cert_id}")
    public ResponseEntity<OrgCertificationDTO> query(
            @ApiParam(value = "组织Id", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "集群Id")
            @PathVariable(value = "cert_id") Long certId) {
        return Optional.ofNullable(devopsOrgCertificationService.getCert(certId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.cert.query"));
    }

    /**
     * 校验证书名唯一性
     *
     * @param organizationId 项目id
     * @param name           集群name
     */
    @Permission(type= ResourceType.ORGANIZATION,roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "校验证书名唯一性")
    @GetMapping(value = "/check_name")
    public void checkName(
            @ApiParam(value = "组织Id", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "集群name", required = true)
            @RequestParam String name) {
        devopsOrgCertificationService.checkName(organizationId, name);
    }

    /**
     * 分页查询项目列表
     *
     * @param organizationId 项目id
     * @return Page
     */
    @Permission(type= ResourceType.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "分页查询项目列表")
    @CustomPageRequest
    @PostMapping("/page_projects")
    public ResponseEntity<PageInfo<ProjectReqVO>> pageProjects(
            @ApiParam(value = "组织ID", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "集群Id")
            @RequestParam(required = false) Long certId,
            @ApiParam(value = "模糊搜索参数")
            @RequestBody String[] params) {
        return Optional.ofNullable(devopsOrgCertificationService.listProjects(organizationId, certId, pageRequest, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.project.query"));
    }

    /**
     * 查询已有权限的项目列表
     *
     * @param organizationId 项目id
     * @return List
     */
    @Permission(type= ResourceType.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "查询已有权限的项目列表")
    @GetMapping("/list_cert_projects/{cert_id}")
    public ResponseEntity<List<ProjectReqVO>> listCertProjects(
            @ApiParam(value = "组织ID", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "集群Id")
            @PathVariable(value = "cert_id") Long certId) {
        return Optional.ofNullable(devopsOrgCertificationService.listCertProjects(certId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.project.query"));
    }


    /**
     * 组织证书列表查询
     *
     * @param organizationId 组织ID
     * @return Page
     */
    @Permission(type= ResourceType.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "组织证书列表查询")
    @CustomPageRequest
    @PostMapping("/page_cert")
    public ResponseEntity<PageInfo<OrgCertificationDTO>> listOrgCert(
            @ApiParam(value = "组织ID", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody String params) {
        return Optional.ofNullable(devopsOrgCertificationService.pageCerts(organizationId, pageRequest, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.cert.query"));
    }

    /**
     * 删除证书
     *
     * @param organizationId 组织ID
     * @param certId         证书Id
     * @return String
     */
    @Permission(type= ResourceType.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "删除证书")
    @CustomPageRequest
    @DeleteMapping("/{cert_id}")
    public ResponseEntity<String> deleteOrgCert(
            @ApiParam(value = "组织ID", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "集群Id")
            @PathVariable(value = "cert_Id") Long certId) {
        devopsOrgCertificationService.deleteCert(certId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
