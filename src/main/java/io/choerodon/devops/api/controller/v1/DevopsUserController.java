package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.annotation.Permission;
import io.choerodon.core.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.UserAttrVO;
import io.choerodon.devops.app.service.UserAttrService;

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
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据用户Id查询gitlab用户Id")
    @GetMapping("/{user_id}")
    public ResponseEntity<UserAttrVO> queryByUserId(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "用户id", required = true)
            @PathVariable(value = "user_id") Long userId) {
        return Optional.ofNullable(userAttrService.queryByUserId(userId))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.user.get"));
    }

    /**
     * 根据多个用户Id查询存在的多个用户信息
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据多个用户Id查询存在的多个用户信息")
    @GetMapping("/list_by_ids")
    public ResponseEntity<List<UserAttrVO>> listByUserIds(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "用户id", required = true)
            @RequestParam(value = "user_ids") Set<Long> iamUserIds) {
        return Optional.ofNullable(userAttrService.listByUserIds(iamUserIds))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.user.get"));
    }
}
