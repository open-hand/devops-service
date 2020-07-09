package io.choerodon.devops.api.controller.v1;

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
import io.choerodon.devops.api.vo.DevopsIngressVO;
import io.choerodon.devops.app.service.DevopsIngressService;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * Creator: Runge
 * Date: 2018/4/20
 * Time: 14:38
 * Description:
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/ingress")
public class DevopsIngressController {

    @Autowired
    private DevopsIngressService devopsIngressService;


    /**
     * 项目下创建域名
     *
     * @param projectId       项目id
     * @param devopsIngressVO 域名信息
     * @return ResponseEntity
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下创建域名")
    @PostMapping
    public ResponseEntity<Void> create(
            @Encrypt
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "域名信息", required = true)
            @RequestBody DevopsIngressVO devopsIngressVO) {
        devopsIngressService.createIngress(projectId, devopsIngressVO);
        return ResponseEntity.noContent().build();
    }

    /**
     * 项目下更新域名
     *
     * @param projectId       项目ID
     * @param id              域名ID
     * @param devopsIngressVO 域名信息
     * @return ResponseEntity
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下更新域名")
    @PutMapping(value = "/{id}")
    public ResponseEntity<Void> update(
            @Encrypt
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "域名ID", required = true)
            @PathVariable Long id,
            @ApiParam(value = "域名信息", required = true)
            @RequestBody DevopsIngressVO devopsIngressVO) {
        devopsIngressService.updateIngress(id, devopsIngressVO, projectId);
        return ResponseEntity.noContent().build();
    }


    /**
     * 项目下查询域名
     *
     * @param projectId 项目ID
     * @param id        域名ID
     * @return DevopsIngressVO
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下查询域名")
    @GetMapping(value = "/{id}")
    public ResponseEntity<DevopsIngressVO> queryIngress(
            @Encrypt
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "域名ID", required = true)
            @PathVariable Long id) {
        return Optional.ofNullable(devopsIngressService.queryIngress(projectId, id))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.IngressName.query"));
    }


    /**
     * 项目下查询域名详情
     *
     * @param projectId 项目ID
     * @param id        域名ID
     * @return DevopsIngressVO
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下查询域名详情")
    @GetMapping(value = "/{id}/detail")
    public ResponseEntity<DevopsIngressVO> queryIngressDetailById(
            @Encrypt
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "域名ID", required = true)
            @PathVariable Long id) {
        return Optional.ofNullable(devopsIngressService.queryIngressDetailById(projectId, id))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.query.by.id"));
    }

    /**
     * 项目下删除域名
     *
     * @param projectId 项目ID
     * @param id        域名ID
     * @return ResponseEntity
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下删除域名")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(
            @Encrypt
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "域名ID", required = true)
            @PathVariable Long id) {
        devopsIngressService.deleteIngress(projectId, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 检查域名唯一性
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "检查域名唯一性")
    @GetMapping(value = "/check_name")
    public ResponseEntity<Boolean> checkName(
            @Encrypt
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "域名名称", required = true)
            @RequestParam String name,
            @ApiParam(value = "环境id", required = true)
            @Encrypt
            @RequestParam(value = "env_id") Long envId) {
        return Optional.ofNullable(devopsIngressService.checkName(envId, name))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.ingress.check"));
    }

    /**
     * 检查域名唯一性
     *
     * @param projectId 项目ID
     * @param domain    域名
     * @param path      路径
     * @param id        ingress ID
     * @return Boolean
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "检查域名名称唯一性")
    @GetMapping(value = "/check_domain")
    public ResponseEntity<Boolean> checkDomain(
            @Encrypt
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境id", required = true)
            @RequestParam(value = "env_id") Long envId,
            @ApiParam(value = "域名", required = true)
            @RequestParam String domain,
            @ApiParam(value = "路径", required = true)
            @RequestParam String path,
            @Encrypt
            @ApiParam(value = "ingress ID")
            @RequestParam(value = "id", required = false) Long id) {
        return Optional.ofNullable(devopsIngressService.checkDomainAndPath(envId, domain, path, id))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.domain.name.check"));
    }


    /**
     * 环境下分页查询域名
     *
     * @param projectId 项目id
     * @param envId     环境Id
     * @param pageable  分页参数
     * @param params    搜索参数
     * @return Page
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @CustomPageRequest
    @ApiOperation(value = "环境下分页查询域名")
    @PostMapping(value = "/{env_id}/page_by_env")
    public ResponseEntity<Page<DevopsIngressVO>> pageByEnv(
            @Encrypt
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.DESC)
            @ApiParam(value = "分页参数") PageRequest pageable,
            @Encrypt
            @ApiParam(value = "env_id", required = true)
            @PathVariable(value = "env_id") Long envId,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return Optional.ofNullable(devopsIngressService.pageByEnv(projectId, envId, pageable, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.appServiceInstance.query"));
    }
}
