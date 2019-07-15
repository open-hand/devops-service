package io.choerodon.devops.api.controller.v1;

import java.util.Optional;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.annotation.Permission;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.DevopsIngressVO;
import io.choerodon.devops.app.service.DevopsIngressService;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Creator: Runge
 * Date: 2018/4/20
 * Time: 14:38
 * Description:
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/ingress")
public class DevopsIngressController {

    private DevopsIngressService devopsIngressService;

    public DevopsIngressController(DevopsIngressService devopsIngressService) {
        this.devopsIngressService = devopsIngressService;
    }


    /**
     * 项目下创建域名
     *
     * @param projectId        项目id
     * @param devopsIngressVO 域名信息
     * @return ResponseEntity
     */
    @Permission(type= ResourceType.PROJECT,roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下创建域名")
    @PostMapping
    public ResponseEntity create(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "域名信息", required = true)
            @RequestBody DevopsIngressVO devopsIngressVO) {
        devopsIngressService.addIngress(devopsIngressVO, projectId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 项目下更新域名
     *
     * @param projectId        项目ID
     * @param id               域名ID
     * @param devopsIngressVO 域名信息
     * @return ResponseEntity
     */
    @Permission(type= ResourceType.PROJECT,roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下更新域名")
    @PutMapping(value = "/{id}")
    public ResponseEntity update(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "域名ID", required = true)
            @PathVariable Long id,
            @ApiParam(value = "域名信息", required = true)
            @RequestBody DevopsIngressVO devopsIngressVO) {
        devopsIngressService.updateIngress(id, devopsIngressVO, projectId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    /**
     * 项目下查询域名
     *
     * @param projectId 项目ID
     * @param id        域名ID
     * @return DevopsIngressVO
     */
    @Permission(type= ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下查询域名")
    @GetMapping(value = "/{id}")
    public ResponseEntity<DevopsIngressVO> queryDomainId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "域名ID", required = true)
            @PathVariable Long id) {
        return Optional.ofNullable(devopsIngressService.getIngress(projectId, id))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.IngressName.query"));
    }

    /**
     * 项目下删除域名
     *
     * @param projectId 项目ID
     * @param id        域名ID
     * @return ResponseEntity
     */
    @Permission(type= ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下删除域名")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity delete(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "域名ID", required = true)
            @PathVariable Long id) {
        devopsIngressService.deleteIngress(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 检查域名唯一性
     */
    @Permission(type= ResourceType.PROJECT,roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "检查域名唯一性")
    @GetMapping(value = "/check_name")
    public ResponseEntity<Boolean> checkName(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "域名名称", required = true)
            @RequestParam String name,
            @ApiParam(value = "域名名称", required = true)
            @RequestParam Long envId) {
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
    @Permission(type= ResourceType.PROJECT,roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "检查域名名称唯一性")
    @GetMapping(value = "/check_domain")
    public ResponseEntity<Boolean> checkDomain(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境id", required = true)
            @RequestParam Long envId,
            @ApiParam(value = "域名", required = true)
            @RequestParam String domain,
            @ApiParam(value = "路径", required = true)
            @RequestParam String path,
            @ApiParam(value = "ingress ID", required = false)
            @RequestParam(value = "id", required = false) Long id) {
        return Optional.ofNullable(devopsIngressService.checkDomainAndPath(envId, domain, path, id))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.domain.name.check"));
    }


    /**
     * 环境总览域名查询
     *
     * @param projectId   项目id
     * @param envId       环境Id
     * @param pageRequest 分页参数
     * @param params      搜索参数
     * @return Page
     */
    @Permission(type= ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @CustomPageRequest
    @ApiOperation(value = "环境总览域名查询")
    @PostMapping(value = "/{envId}/listByEnv")
    public ResponseEntity<PageInfo<DevopsIngressVO>> listByEnv(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiIgnore
            @ApiParam(value = "分页参数") PageRequest pageRequest,
            @ApiParam(value = "envId", required = true)
            @PathVariable(value = "envId") Long envId,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return Optional.ofNullable(devopsIngressService.listByEnv(projectId, envId, pageRequest, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.appInstance.query"));
    }
}
