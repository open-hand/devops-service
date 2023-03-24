package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.C7nCertificationCreateOrUpdateVO;
import io.choerodon.devops.api.vo.CertificationRespVO;
import io.choerodon.devops.api.vo.CertificationVO;
import io.choerodon.devops.api.vo.ProjectCertificationVO;
import io.choerodon.devops.app.service.CertificationService;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * 创建环境中的证书对象的controller
 * Created by n!Ck
 * Date: 2018/8/20
 * Time: 16:59
 * Description:
 */
@Api("环境中的证书对象")
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/certifications")
public class CertificationController {

    @Autowired
    private CertificationService certificationService;

    /**
     * 项目下创建证书
     *
     * @param projectId     项目id
     * @param certification 证书名
     * @return 201, "Created"
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下创建证书")
    @PostMapping
    public ResponseEntity<Void> create(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "证书", required = true)
            @ModelAttribute C7nCertificationCreateOrUpdateVO certification,
            @ApiParam(value = "key文件")
            @RequestParam(value = "key", required = false) MultipartFile key,
            @ApiParam(value = "cert文件")
            @RequestParam(value = "cert", required = false) MultipartFile cert) {
        certificationService.createCertification(projectId, certification, key, cert);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 项目下删除证书
     *
     * @param projectId 项目id
     * @param certId    证书id
     * @return 204, "No Content"
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下删除证书")
    @DeleteMapping
    public ResponseEntity<Void> delete(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "证书id", required = true)
            @RequestParam(value = "cert_id") Long certId) {
        certificationService.deleteById(projectId, certId);
        return ResponseEntity.noContent().build();
    }


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
    @ApiOperation(value = "分页查询证书")
    @CustomPageRequest
    @PostMapping("/page_by_options")
    public ResponseEntity<Page<CertificationVO>> pageByOptions(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境ID")
            @RequestParam(value = "env_id", required = false) Long envId,
            @ApiParam(value = "分页参数")
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.ASC) PageRequest pageable,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return ResponseEntity.ok(certificationService.pageByOptions(projectId, envId, pageable, params));
    }

    /**
     * 通过域名查询已生效的证书
     *
     * @param projectId 项目id
     * @param domain    域名
     * @return CertificationVO baseList
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "通过域名查询已生效的证书")
    @PostMapping("/active")
    public ResponseEntity<List<CertificationVO>> getActiveCertificationByDomain(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境ID", required = true)
            @RequestParam(value = "env_id") Long envId,
            @ApiParam(value = "域名")
            @RequestParam(value = "domain") String domain) {
        return ResponseEntity.ok(certificationService.queryActiveCertificationByDomain(projectId, envId, domain));
    }

    /**
     * 校验证书名称唯一性
     *
     * @param projectId 项目id
     * @param envId     环境ID
     * @param certName  证书名称
     * @return Boolean
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "校验证书名称唯一性")
    @GetMapping("/unique")
    public ResponseEntity<Boolean> checkCertNameUniqueInEnv(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境ID", required = true)
            @RequestParam(value = "env_id") Long envId,
            @ApiParam(value = "证书名称", required = true)
            @RequestParam(value = "cert_name") String certName) {
        return ResponseEntity.ok(certificationService.checkCertNameUniqueInEnv(envId, certName));
    }


    /**
     * 根据证书名称查询证书
     *
     * @param projectId 项目id
     * @param envId     环境ID
     * @param certName  证书名称
     * @return CertificationVO
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据证书名称查询证书")
    @GetMapping("/query_by_name")
    public ResponseEntity<CertificationVO> queryByName(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境ID", required = true)
            @RequestParam(value = "env_id") Long envId,
            @ApiParam(value = "证书名称", required = true)
            @RequestParam(value = "cert_name") String certName) {
        return ResponseEntity.ok(certificationService.queryByName(envId, certName));
    }


    /**
     * 根据证书ID查询证书
     *
     * @param projectId 项目id
     * @param certId    证书ID
     * @return CertificationVO
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据证书ID查询证书")
    @GetMapping("/{cert_id}")
    public ResponseEntity<CertificationRespVO> queryById(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "证书ID", required = true)
            @PathVariable(value = "cert_id") Long certId) {
        return ResponseEntity.ok(certificationService.queryByCertId(certId));
    }

    /**
     * 查询项目下有权限的组织层证书
     *
     * @param projectId 项目id
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询项目下有权限的组织层证书")
    @GetMapping("/list_org_cert")
    public ResponseEntity<List<ProjectCertificationVO>> listOrgCert(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return ResponseEntity.ok(certificationService.listProjectCertInProject(projectId));
    }

}