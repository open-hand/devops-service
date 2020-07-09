package io.choerodon.devops.api.controller.v1;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.CiVariableVO;
import io.choerodon.devops.app.service.DevopsCiVariableService;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ci 变量
 *
 * @author lihao
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/ci_variable")
public class DevopsCiVariableController {

    @Autowired
    private DevopsCiVariableService devopsCiVariableService;

    /**
     * 列举出ci变量,只有key
     *
     * @param projectId 项目id
     * @return ci变量
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "列举出ci变量")
    @GetMapping("/keys")
    public ResponseEntity<Map<String, List<CiVariableVO>>> listVariableKey(
            @Encrypt
            @ApiParam(value = "项目Id", required = true)
            @PathVariable("project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "应用Id")
            @RequestParam(value = "app_service_id", required = false) Long appServiceId) {
        return Optional.ofNullable(devopsCiVariableService.listKeys(projectId, appServiceId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.devops.ci.variable.key.list"));
    }

    /**
     * @param projectId    项目id
     * @param appServiceId 应用服务id
     * @return 返回key、value键值对
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "列举出指定key的value")
    @GetMapping("/values")
    public ResponseEntity<List<CiVariableVO>> listValues(
            @Encrypt
            @ApiParam(value = "项目Id", required = true)
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "层级", required = true)
            @RequestParam("level") String level,
            @Encrypt
            @ApiParam(value = "应用Id")
            @RequestParam(value = "app_service_id", required = false) Long appServiceId) {
        return Optional.ofNullable(devopsCiVariableService.listValues(projectId, level, appServiceId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.devops.ci.variable.value.list"));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "保存对key的修改")
    @PostMapping
    public ResponseEntity<Void> save(
            @Encrypt
            @ApiParam(value = "项目Id", required = true)
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "层级", required = true)
            @RequestParam("level") String level,
            @ApiParam(value = "应用Id")
            @Encrypt
            @RequestParam(value = "app_service_id", required = false) Long appServiceId,
            @ApiParam(value = "变量列表")
            @RequestBody List<CiVariableVO> ciVariableVOList) {
        devopsCiVariableService.save(projectId, level, appServiceId, ciVariableVOList);
        return ResponseEntity.noContent().build();
    }
}

