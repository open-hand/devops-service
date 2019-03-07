package io.choerodon.devops.api.controller.v1;

import java.util.Optional;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.dto.UserAttrDTO;
import io.choerodon.devops.app.service.UserAttrService;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/v1/projects/{project_id}/users")
public class DevopsUserController {

    @Autowired
    private UserAttrService userAttrService;

    /**
     * 根据用户Id查询gitlab用户Id
     *
     * @return UserAttrDTO
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据用户Id查询gitlab用户Id")
    @GetMapping("/{user_id}")
    public ResponseEntity<UserAttrDTO> createOrUpdate(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "用户id", required = true)
            @PathVariable(value = "user_id") Long userId) {
        return Optional.ofNullable(userAttrService.queryByUserId(userId))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.user.get"));
    }
}
