package io.choerodon.devops.api.controller.v1;

import java.util.Optional;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.dto.UserAttrDTO;
import io.choerodon.devops.app.service.UserAttrService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @Permission(type= ResourceType.PROJECT,roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
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
