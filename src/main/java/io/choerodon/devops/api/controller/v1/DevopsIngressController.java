package io.choerodon.devops.api.controller.v1;

import java.util.Optional;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.dto.DevopsIngressDTO;
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
@RequestMapping(value = "/v1/project/{projectId}/ingress")
public class DevopsIngressController {

    private DevopsIngressService devopsIngressService;

    public DevopsIngressController(DevopsIngressService devopsIngressService) {
        this.devopsIngressService = devopsIngressService;
    }


    /**
     * 项目下创建域名
     *
     * @param projectId        项目id
     * @param devopsIngressDTO 域名信息
     * @return ResponseEntity
     */
    @Permission(level = ResourceLevel.PROJECT)
    @ApiOperation(value = "项目下创建域名")
    @PostMapping
    public ResponseEntity create(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable Long projectId,
            @ApiParam(value = "域名信息", required = true)
            @RequestBody DevopsIngressDTO devopsIngressDTO) {
        devopsIngressService.addIngress(devopsIngressDTO, projectId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 项目下更新域名
     *
     * @param projectId        项目ID
     * @param id               域名ID
     * @param devopsIngressDTO 域名信息
     * @return ResponseEntity
     */
    @Permission(level = ResourceLevel.PROJECT)
    @ApiOperation(value = "项目下更新域名")
    @PutMapping(value = "/{id}")
    public ResponseEntity update(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable Long projectId,
            @ApiParam(value = "域名ID", required = true)
            @PathVariable Long id,
            @ApiParam(value = "域名信息", required = true)
            @RequestBody DevopsIngressDTO devopsIngressDTO) {
        devopsIngressService.updateIngress(id, devopsIngressDTO, projectId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 项目下查询域
     *
     * @param projectId   项目ID
     * @param pageRequest 分页参数
     * @param params      查询参数
     * @return Page of DevopsIngressDTO
     */
    @Permission(level = ResourceLevel.PROJECT)
    @ApiOperation(value = "项目下查询所有域名")
    @CustomPageRequest
    @PostMapping(value = "/list_by_options")
    public ResponseEntity<Page<DevopsIngressDTO>> pageByOptions(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable Long projectId,
            @ApiParam(value = "分页参数")
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.ASC) PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return Optional.ofNullable(devopsIngressService.getIngress(projectId, pageRequest, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.appTemplate.create"));
    }

    /**
     * 项目下查询域名
     *
     * @param projectId 项目ID
     * @param id        域名ID
     * @return DevopsIngressDTO
     */
    @Permission(level = ResourceLevel.PROJECT)
    @ApiOperation(value = "项目下查询域名")
    @GetMapping(value = "/{id}")
    public ResponseEntity<DevopsIngressDTO> queryDomainId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable Long projectId,
            @ApiParam(value = "域名ID", required = true)
            @PathVariable Long id) {
        return Optional.ofNullable(devopsIngressService.getIngress(projectId, id))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.appTemplate.create"));
    }

    /**
     * 项目下删除域名
     *
     * @param projectId 项目ID
     * @param id        域名ID
     * @return ResponseEntity
     */
    @Permission(level = ResourceLevel.PROJECT)
    @ApiOperation(value = "项目下删除域名")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity delete(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable Long projectId,
            @ApiParam(value = "域名ID", required = true)
            @PathVariable Long id) {
        devopsIngressService.deleteIngress(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 检查域名唯一性
     */
    @Permission(level = ResourceLevel.PROJECT)
    @ApiOperation(value = "检查域名唯一性")
    @GetMapping(value = "/check_name")
    public ResponseEntity<Boolean> checkName(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable Long projectId,
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
     * @return Boolean
     */
    @Permission(level = ResourceLevel.PROJECT)
    @ApiOperation(value = "检查域名名称唯一性")
    @GetMapping(value = "/check_domain")
    public ResponseEntity<Boolean> checkDomain(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable Long projectId,
            @ApiParam(value = "域名", required = true)
            @RequestParam String domain,
            @ApiParam(value = "路径", required = true)
            @RequestParam String path) {
        return Optional.ofNullable(devopsIngressService.checkDomainAndPath(domain, path))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.domain.name.check"));
    }
}
