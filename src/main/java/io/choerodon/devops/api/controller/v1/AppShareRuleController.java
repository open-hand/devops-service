package io.choerodon.devops.api.controller.v1;

import java.util.Optional;
import javax.validation.Valid;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.AppServiceShareRuleUpdateVO;
import io.choerodon.devops.api.vo.AppServiceShareRuleVO;
import io.choerodon.devops.app.service.AppServiceShareRuleService;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * Created by ernst on 2018/5/12.
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/app_service_share")
public class AppShareRuleController {
    private final AppServiceShareRuleService applicationShareService;

    public AppShareRuleController(AppServiceShareRuleService applicationShareService) {
        this.applicationShareService = applicationShareService;
    }


    /**
     * 创建服务共享规则
     *
     * @param projectId             项目id
     * @param appServiceShareRuleVO 服务共享规则
     * @return Long
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "创建服务共享规则")
    @PostMapping
    public ResponseEntity<AppServiceShareRuleVO> create(
            @Encrypt
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "服务共享规则", required = true)
            @RequestBody @Valid AppServiceShareRuleVO appServiceShareRuleVO) {
        return Optional.ofNullable(
                applicationShareService.createOrUpdate(projectId, appServiceShareRuleVO))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.share.rule.create"));
    }

    /**
     * 更新服务共享规则
     *
     * @param projectId                   项目id
     * @param appServiceShareRuleUpdateVO 服务共享规则
     * @return Long
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "更新服务共享规则")
    @PutMapping
    public ResponseEntity<AppServiceShareRuleVO> update(
            @Encrypt
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "服务共享规则", required = true)
            @RequestBody @Valid AppServiceShareRuleUpdateVO appServiceShareRuleUpdateVO) {
        return Optional.ofNullable(
                applicationShareService.createOrUpdate(projectId, ConvertUtils.convertObject(appServiceShareRuleUpdateVO, AppServiceShareRuleVO.class)))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.share.rule.update"));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "分页查询服务共享规则")
    @PostMapping(value = "/page_by_options")
    @CustomPageRequest
    public ResponseEntity<Page<AppServiceShareRuleVO>> pageByOptions(
            @Encrypt
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam
            @RequestParam(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageable,
            @ApiParam(value = "过滤参数")
            @RequestBody(required = false) String param) {
        return Optional.ofNullable(
                applicationShareService.pageByOptions(projectId, appServiceId, pageable, param))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.share.rule.page"));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询单个共享规则详情")
    @GetMapping(value = "/{rule_id}")
    public ResponseEntity<AppServiceShareRuleVO> query(
            @Encrypt
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "规则Id", required = true)
            @PathVariable(value = "rule_id") Long ruleId) {
        return Optional.ofNullable(
                applicationShareService.query(projectId, ruleId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.share.rule.query"));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "删除单个服务共享规则")
    @DeleteMapping(value = "/{rule_id}")
    public ResponseEntity<Void> delete(
            @Encrypt
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "规则Id", required = true)
            @PathVariable(value = "rule_id") Long ruleId) {
        applicationShareService.delete(projectId, ruleId);
        return ResponseEntity.noContent().build();
    }
}
