package io.choerodon.devops.api.controller.v1;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.DevopsPvcReqVO;
import io.choerodon.devops.app.service.DevopsPvcService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping(value = "/v1/projects/{project_id}")
public class DevopsPvcController {
    @Autowired
    DevopsPvcService devopsPvcService;

    @PostMapping
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "创建PVC")
    public ResponseEntity create(
            @ApiParam(value = "项目id", required = true)
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "PVC信息", required = true)
            @RequestBody @Valid DevopsPvcReqVO devopsPvcReqVO) {
        devopsPvcService.create(projectId, devopsPvcReqVO);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
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
                .orElseThrow(() -> new CommonException("error.secret.delete"));
    }
}
