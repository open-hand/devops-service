package io.choerodon.devops.api.controller.v1;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.DevopsEnvPodVO;
import io.choerodon.devops.app.service.DevopsEnvPodService;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsEnvPodDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.DevopsServiceInstanceDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Optional;

/**
 * Created by Zenger on 2018/4/17.
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/pods")
public class DevopsEnvPodController {

    @Autowired
    private DevopsEnvPodService devopsEnvPodService;

    /**
     * 分页查询环境下pod
     *
     * @param projectId   项目id
     * @param pageable    分页参数
     * @param searchParam 查询参数
     * @return page of DevopsEnvironmentPodVO
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "分页查询环境下pod")
    @CustomPageRequest
    @PostMapping(value = "/page_by_options")
    public ResponseEntity<Page<DevopsEnvPodVO>> pageByOptions(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageable,
            @ApiParam(value = "环境id")
            @RequestParam(value = "env_id", required = false) Long envId,
            @ApiParam(value = "应用id")
            @Encrypt(AppServiceDTO.ENCRYPT_KEY) @RequestParam(value = "app_service_id", required = false) Long appServiceId,
            @ApiParam(value = "应用id")
            @Encrypt(DevopsServiceInstanceDTO.ENCRYPT_KEY) @RequestParam(value = "instance_id", required = false) Long instanceId,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String searchParam) {
        return Optional.ofNullable(devopsEnvPodService.pageByOptions(
                projectId, envId, appServiceId, instanceId, pageable, searchParam))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.application.pod.query"));
    }

//    /**
//     * 按资源用量列出环境下Pod信息
//     *
//     * @param envId 环境id
//     * @param sort  排序条件
//     * @return 环境下相关资源的数量
//     */
//    @Permission(level = ResourceLevel.ORGANIZATION,
//            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
//    @ApiOperation(value = "按资源用量列出环境下Pod信息")
//    @GetMapping("/pod_ranking")
//    public ResponseEntity<List<DevopsEnvPodInfoVO>> queryEnvPodInfo(
//            @ApiParam(value = "项目id")
//            @PathVariable(value = "project_id") Long projectId,
//            @ApiParam(value = "环境id", required = true)
//            @RequestParam(value = "env_id") Long envId,
//            @ApiParam(value = "排序方式")
//            @RequestParam(value = "sort", required = false, defaultValue = "memory") String sort) {
//        return Optional.ofNullable(devopsEnvPodService.queryEnvPodInfo(envId, sort))
//                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
//                .orElseThrow(() -> new CommonException("error.pod.ranking.query"));
//    }

    /**
     * 删除实例下面的pod
     *
     * @param envId 环境id
     * @param podId pod id
     * @return void
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "删除环境下的pod")
    @DeleteMapping("/{pod_id}")
    public ResponseEntity<Void> deleteEnvPod(
            @ApiParam(value = "项目id")
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "podId")
            @Encrypt(DevopsEnvPodDTO.ENCRYPT_KEY) @PathVariable(value = "pod_id") Long podId,
            @ApiParam(value = "环境id", required = true)
            @Encrypt(DevopsEnvironmentDTO.ENCRYPT_KEY) @RequestParam(value = "env_id") Long envId) {
        devopsEnvPodService.deleteEnvPodById(projectId, envId, podId);
        return ResponseEntity.noContent().build();
    }
}
