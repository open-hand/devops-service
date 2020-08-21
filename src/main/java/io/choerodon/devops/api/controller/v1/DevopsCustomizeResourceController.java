package io.choerodon.devops.api.controller.v1;

import java.util.Objects;
import java.util.Optional;
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
import io.choerodon.devops.api.vo.DevopsCustomizeResourceCreateOrUpdateVO;
import io.choerodon.devops.api.vo.DevopsCustomizeResourceVO;
import io.choerodon.devops.app.service.DevopsCustomizeResourceService;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * Created by Sheep on 2019/6/26.
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/customize_resource")
public class DevopsCustomizeResourceController {

    @Autowired
    private DevopsCustomizeResourceService devopsCustomizeResourceService;

    /**
     * 创建或更新其他k8s资源
     *
     * @param projectId   项目id
     * @param contentFile 内容文件
     * @return 201状态码
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "创建或更新其他k8s资源")
    @PostMapping
    public ResponseEntity<Void> createResource(
            @PathVariable(value = "project_id") Long projectId,
            @ModelAttribute @Valid DevopsCustomizeResourceCreateOrUpdateVO devopsCustomizeResourceReqVO,
            BindingResult bindingResult,
            @RequestParam(value = "contentFile", required = false) MultipartFile contentFile) {
        // 底层不能捕获BindException异常，所以这里手动处理抛出CommonException
        if (bindingResult.hasErrors()) {
            throw new CommonException(Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }
        devopsCustomizeResourceService.createOrUpdateResource(projectId, devopsCustomizeResourceReqVO, contentFile);
        return ResponseEntity.ok().build();
    }

    /**
     * 删除其他k8s资源
     *
     * @param projectId  项目id
     * @param resourceId 资源的id
     * @return 204
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "删除其他k8s资源")
    @DeleteMapping
    public ResponseEntity<Void> deleteResource(
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @RequestParam(value = "resource_id") Long resourceId) {
        devopsCustomizeResourceService.deleteResource(projectId, resourceId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 获取资源详情
     *
     * @param projectId  项目id
     * @param resourceId 资源id
     * @return 资源
     */

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取资源详情")
    @GetMapping("/{resource_id}")
    public ResponseEntity<DevopsCustomizeResourceVO> getResource(
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @PathVariable(value = "resource_id") Long resourceId) {
        return Optional.ofNullable(devopsCustomizeResourceService.queryDevopsCustomizeResourceDetail(resourceId))
                .map(t -> new ResponseEntity<>(t, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.resource.get"));
    }


    /**
     * 其他K8S资源分页查询
     *
     * @param projectId   项目id
     * @param envId       环境id
     * @param pageable    分页参数
     * @param searchParam 查询参数
     * @return Page of DevopsCustomizeResourceDTO
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "其他K8S资源分页查询")
    @CustomPageRequest
    @PostMapping(value = "/{env_id}/page_by_env")
    public ResponseEntity<Page<DevopsCustomizeResourceVO>> pageByEnv(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境id", required = true)
            @PathVariable(value = "env_id") Long envId,
            @ApiParam(value = "分页参数")
            @SortDefault(value = "id", direction = Sort.Direction.DESC)
            @ApiIgnore PageRequest pageable,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String searchParam) {
        return Optional.ofNullable(devopsCustomizeResourceService.pageResources(envId, pageable, searchParam))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.resource.query"));
    }
}
