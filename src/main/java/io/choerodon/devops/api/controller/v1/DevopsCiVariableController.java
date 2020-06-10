package io.choerodon.devops.api.controller.v1;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.CiVariableVO;
import io.choerodon.devops.app.service.DevopsCiVariableService;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
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
     * 列举出全局ci变量
     *
     * @param projectId 项目id
     * @return 全局ci变量
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "列举出全局ci变量")
    @GetMapping
    public ResponseEntity<List<CiVariableVO>> listGlobalVariable(@PathVariable("project_id") Long projectId) {
        return Optional.ofNullable(devopsCiVariableService.listGlobalVariable(projectId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.devops.ci.global.variable.list"));
    }

    /**
     * 列举出应用ci变量
     *
     * @param projectId    项目id
     * @param appServiceId 应用id
     * @return 应用ci变量
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = " 列举出应用ci变量")
    @GetMapping("/app_service/{app_service_id}")
    public ResponseEntity<List<CiVariableVO>> listAppServiceVariable(@PathVariable("project_id") Long projectId, @PathVariable("app_service_id") Long appServiceId) {
        return Optional.ofNullable(devopsCiVariableService.listAppServiceVariable(projectId, appServiceId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.devops.ci.appService.variable.list"));
    }
}
