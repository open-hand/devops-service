package io.choerodon.devops.api.controller.v1;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.dto.DevopsAppResourceDTO;
import io.choerodon.devops.app.service.DevopsAppResourceService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * @author lizongwei
 * @date 2019/7/3
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/app/resource")
public class DevopsAppResourceController {

    @Autowired
    DevopsAppResourceService appResourceService;

    /**
     * 创建应用资源关系
     *
     * @param projectId            项目id
     * @param devopsAppResourceDTO 应用资源关系
     * @return
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "创建应用资源关系")
    @PostMapping
    public ResponseEntity createAppResource(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用资源关系", required = true)
            @RequestBody DevopsAppResourceDTO devopsAppResourceDTO) {
        appResourceService.insert(devopsAppResourceDTO);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    /**
     * 通过应用和类型删除应用资源关系
     *
     * @param projectId 项目id
     * @param appId     应用id
     * @param type      类型
     * @return
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "通过应用和类型删除应用资源关系")
    @DeleteMapping("/{app_id}")
    public ResponseEntity deleteByAppIdAndType(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用id", required = true)
            @PathVariable("app_id") Long appId,
            @ApiParam(value = "类型", required = true)
            @RequestParam("type") String type) {
        appResourceService.deleteByAppIdAndType(appId, type);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    /**
     * 查询应用下的资源
     *
     * @param projectId 项目id
     * @param appId     应用id
     * @param type      类型
     * @return List
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询应用下的资源")
    @GetMapping("/{app_id}")
    public ResponseEntity<List<DevopsAppResourceDTO>> queryByAppAndType(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用id", required = true)
            @PathVariable("app_id") Long appId,
            @ApiParam(value = "类型", required = true)
            @RequestParam("type") String type) {
        return Optional.ofNullable(appResourceService.queryByAppAndType(appId, type))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.app.resource.query"));
    }

}
