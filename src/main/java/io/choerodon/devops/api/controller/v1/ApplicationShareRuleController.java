package io.choerodon.devops.api.controller.v1;

import java.util.Optional;

import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.ApplicationShareRuleVO;
import io.choerodon.devops.app.service.ApplicationShareRuleService;
import io.choerodon.swagger.annotation.CustomPageRequest;

/**
 * Created by ernst on 2018/5/12.
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/apps_share")
public class ApplicationShareRuleController {
    private ApplicationShareRuleService applicationShareService;

    public ApplicationShareRuleController(ApplicationShareRuleService applicationShareService) {
        this.applicationShareService = applicationShareService;
    }


    /**
     * 创建应用共享规则
     *
     * @param projectId              项目id
     * @param applicationShareRuleVO 应用共享规则
     * @return Long
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "应用共享规则")
    @PostMapping
    public ResponseEntity<ApplicationShareRuleVO> createOrUpdate(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用共享规则", required = true)
            @RequestBody ApplicationShareRuleVO applicationShareRuleVO) {
        return Optional.ofNullable(
                applicationShareService.createOrUpdate(projectId, applicationShareRuleVO))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.share.rule.create"));
    }

    /**
     * 查询应用共享规则
     *
     * @param projectId
     * @param pageRequest
     * @param param
     * @return
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "应用共享规则")
    @PostMapping(value = "/{app_service_id}/page_by_options")
    @CustomPageRequest
    public ResponseEntity<PageInfo<ApplicationShareRuleVO>> pageByOptions(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam
            @PathVariable(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "过滤参数")
            @RequestBody(required = false) String param) {
        return Optional.ofNullable(
                applicationShareService.pageByOptions(appServiceId, pageRequest, param))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.share.rule.page"));
    }

    /**
     * 查询单个应用共享规则详情
     *
     * @param projectId
     * @param ruleId
     * @return
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "应用共享规则")
    @GetMapping(value = "/{rule_id}")
    public ResponseEntity<ApplicationShareRuleVO> query(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "规则Id", required = true)
            @PathVariable(value = "rule_id") Long ruleId) {
        return Optional.ofNullable(
                applicationShareService.query(projectId, ruleId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.share.rule.query"));
    }


}
