package io.choerodon.devops.api.controller.v1;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.annotation.Permission;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.DevopsPvcReqVO;
import io.choerodon.devops.api.vo.DevopsPvcRespVO;
import io.choerodon.devops.app.service.DevopsPvcService;

import io.choerodon.swagger.annotation.CustomPageRequest;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping(value = "/v1/projects/{project_id}/pvc")
public class DevopsPvcController {
    @Autowired
    DevopsPvcService devopsPvcService;

    /**
     * 分页查询
     *
     * @param projectId   项目id
     * @param envId       环境ID
     * @param pageable 分页参数
     * @param params      查询参数
     * @return CertificationDTO page
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "分页查询")
    @CustomPageRequest
    @PostMapping("/page_by_options")
    public ResponseEntity<PageInfo<DevopsPvcRespVO>> pageByOptions(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境ID")
            @RequestParam(value = "env_id", required = false) Long envId,
            @ApiParam(value = "分页参数")
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.ASC) Pageable pageable,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return Optional.ofNullable(devopsPvcService.pageByOptions(projectId, envId, pageable, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.pvc.page"));
    }

    /**
     * 创建PVC
     *
     * @param projectId
     * @param devopsPvcReqVO
     * @return
     */
    @PostMapping
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "创建PVC")
    public ResponseEntity<DevopsPvcRespVO> create(
            @ApiParam(value = "项目id", required = true)
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "PVC信息", required = true)
            @RequestBody @Valid DevopsPvcReqVO devopsPvcReqVO) {
        return Optional.ofNullable(devopsPvcService.create(projectId, devopsPvcReqVO))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.pvc.create"));
    }

    /**
     * 删除PVC
     *
     * @param envId 环境id
     * @param pvcId PVC id
     * @return Boolean
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "删除PVC")
    @DeleteMapping("/{env_id}/{pvc_id}")
    public ResponseEntity<Boolean> deleteSecret(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境id", required = true)
            @PathVariable(value = "env_id") Long envId,
            @ApiParam(value = "PVC id", required = true)
            @PathVariable(value = "pvc_id") Long pvcId) {
        return Optional.of(devopsPvcService.delete(envId, pvcId))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.pvc.delete"));
    }

    /**
     * 检查PVC名称的唯一性
     *
     * @param name  PVC名称
     * @param envId 环境id
     * @return Boolean
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "删除PVC")
    @GetMapping("/check_name")
    public void deleteSecret(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境id", required = true)
            @RequestParam(value = "env_id") Long envId,
            @ApiParam(value = "PVC名称", required = true)
            @RequestParam(value = "name") String name) {
        devopsPvcService.baseCheckName(name, envId);
    }

}
