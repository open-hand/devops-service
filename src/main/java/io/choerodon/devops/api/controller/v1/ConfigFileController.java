package io.choerodon.devops.api.controller.v1;


import javax.validation.Valid;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.ConfigFileVO;
import io.choerodon.devops.app.service.ConfigFileService;
import io.choerodon.devops.infra.dto.ConfigFileDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;


/**
 * 配置文件表(ConfigFile)表控制层
 *
 * @author hao.wang@zknow.com
 * @since 2023-02-15 09:25:06
 */

@RestController("configFileController.v1")
@RequestMapping("/v1/project/{project_id}/config_files")
public class ConfigFileController {

    @Autowired
    private ConfigFileService configFileService;

    @ApiOperation(value = "新增配置文件")
    @PostMapping
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<ConfigFileDTO> create(
            @PathVariable(value = "project_id") Long projectId,
            @Valid @RequestBody ConfigFileVO configFileVO) {
        return ResponseEntity.ok(configFileService.create(ResourceLevel.PROJECT.value(),
                projectId,
                configFileVO));
    }

    @ApiOperation(value = "修改配置文件")
    @PutMapping("{id}")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<Void> update(
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @PathVariable(value = "id") Long id,
            @RequestBody ConfigFileVO configFileVO) {
        configFileService.update(ResourceLevel.PROJECT.value(),
                projectId,
                id,
                configFileVO);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "删除配置文件")
    @DeleteMapping("{id}")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<Void> delete(
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @PathVariable(value = "id") Long id) {
        configFileService.delete(ResourceLevel.PROJECT.value(),
                projectId,
                id);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "分页查询配置文件列表")
    @GetMapping("/paging")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @CustomPageRequest
    public ResponseEntity<Page<ConfigFileVO>> paging(
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageable,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String params) {
        return ResponseEntity.ok(configFileService.paging(ResourceLevel.PROJECT.value(),
                projectId,
                pageable,
                name,
                description,
                params));
    }

    @ApiOperation(value = "查询配置文件详情")
    @GetMapping("{id}")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<ConfigFileVO> query(
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @PathVariable(value = "id") Long id) {
        return ResponseEntity.ok(configFileService.queryByIdWithDetail(id));
    }

    @ApiOperation(value = "校验配置文件是否存在引用关系")
    @GetMapping("/{id}/check_is_used")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<Boolean> checkIsUsed(
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @PathVariable(value = "id") Long id) {
        return ResponseEntity.ok(configFileService.checkIsUsed(projectId, id));
    }

}

