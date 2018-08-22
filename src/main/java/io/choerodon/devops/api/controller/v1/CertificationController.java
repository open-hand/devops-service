package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Optional;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.dto.CertificationDTO;
import io.choerodon.devops.app.service.CertificationService;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * Created by n!Ck
 * Date: 2018/8/20
 * Time: 16:59
 * Description:
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/envs/{env_id}/certifications")
public class CertificationController {

    @Autowired
    private CertificationService certificationService;

    /**
     * 项目下创建证书
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @param name      证书名字
     * @param type      证书类型
     * @param domains   域名
     * @param key       key文件
     * @param cert      cert文件
     * @return 204, "No Content"
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "项目下创建证书")
    @PostMapping
    public ResponseEntity create(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境id")
            @PathVariable(value = "env_id") Long envId,
            @ApiParam(value = "证书名字")
            @RequestParam(value = "name") String name,
            @ApiParam(value = "证书类型", required = true)
            @RequestParam(value = "type") String type,
            @ApiParam(value = "域名", required = true)
            @RequestParam(value = "domains") List<String> domains,
            @ApiParam(value = "key文件")
            @RequestParam(value = "keyFile", required = false) MultipartFile key,
            @ApiParam(value = "cert文件")
            @RequestParam(value = "certFile", required = false) MultipartFile cert) {
        certificationService.create(projectId, envId, name, type, domains, key, cert);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 项目下删除证书
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @param certId    证书id
     * @return 204, "No Content"
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "项目下删除证书")
    @DeleteMapping
    public ResponseEntity delete(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境id")
            @PathVariable(value = "env_id") Long envId,
            @ApiParam(value = "证书id")
            @RequestParam(value = "cert_id") Long certId) {
        certificationService.deleteById(certId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    /**
     * 分页查询
     *
     * @param projectId   项目id
     * @param envId       环境id
     * @param pageRequest 分页参数
     * @param params      查询参数
     * @return CertificationDTO page
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_OWNER, InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "分页查询")
    @CustomPageRequest
    @PostMapping("/list_by_options")
    public ResponseEntity<Page<CertificationDTO>> listByOptions(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境id", required = true)
            @PathVariable(value = "env_id") Long envId,
            @ApiParam(value = "分页参数")
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.ASC) PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return Optional.ofNullable(certificationService.getByEnvid(pageRequest, envId, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.certification.page"));
    }

    /**
     * 通过域名查询已生效的证书
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @param domain    域名
     * @return CertificationDTO list
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "通过域名查询已生效的证书")
    @CustomPageRequest
    @PostMapping("/active")
    public ResponseEntity<List<CertificationDTO>> getActiveByDomain(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境id", required = true)
            @PathVariable(value = "env_id") Long envId,
            @ApiParam(value = "域名")
            @RequestParam(value = "domain") String domain) {
        return Optional.ofNullable(certificationService.getActiveByDomain(envId, domain))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.certification.queryByDomain"));
    }
}
