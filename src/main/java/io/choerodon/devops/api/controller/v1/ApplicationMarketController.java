package io.choerodon.devops.api.controller.v1;

import java.util.Optional;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.dto.ApplicationReleasingDTO;
import io.choerodon.devops.app.service.ApplicationMarketService;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * Created by ernst on 2018/5/12.
 */
@RestController
@RequestMapping(value = "/v1/project/{projectId}/apps_market")
public class ApplicationMarketController {

    private ApplicationMarketService applicationMarketService;

    public ApplicationMarketController(ApplicationMarketService applicationMarketService) {
        this.applicationMarketService = applicationMarketService;
    }

    /**
     * 应用发布
     *
     * @param projectId             项目id
     * @param applicationReleaseDTO 发布应用的信息
     * @return
     */
    @Permission(level = ResourceLevel.PROJECT)
    @ApiOperation(value = "应用发布")
    @PostMapping
    public ResponseEntity<Long> create(
            @ApiParam(value = "项目id", required = true)
            @PathVariable Long projectId,
            @ApiParam(value = "发布应用的信息", required = true)
            @RequestBody(required = true) ApplicationReleasingDTO applicationReleaseDTO) {
        return Optional.ofNullable(
                applicationMarketService.release(projectId, applicationReleaseDTO))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.market.release"));
    }

    /**
     * 应用发布上传图片
     *
     * @param projectId   项目id
     * @param appMarketId 应用市场ID
     * @return
     */
    @Permission(level = ResourceLevel.PROJECT)
    @ApiOperation(value = "应用发布上传图片")
    @PostMapping(value = "/upload/{appMarketId}")
    public ResponseEntity<Boolean> upload(
            @ApiParam(value = "项目id", required = true)
            @PathVariable Long projectId,
            @ApiParam(value = "应用市场ID", required = true)
            @PathVariable Long appMarketId,
            @ApiParam(value = "上传的图片", required = true)
            @RequestPart(name = "file", required = true) MultipartFile multipartFile) {
        applicationMarketService.uploadPic(projectId, appMarketId, multipartFile);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 项目下查询所有发布在应用市场的应用
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @param searchParam 搜索参数
     * @return list of ApplicationReleasingDTO
     */
    @Permission(level = ResourceLevel.PROJECT)
    @ApiOperation(value = "项目下分页查询所有发布在应用市场的应用")
    @CustomPageRequest
    @PostMapping(value = "/list")
    public ResponseEntity<Page<ApplicationReleasingDTO>> pageListMarketAppsByProjectId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable Long projectId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String searchParam) {
        return Optional.ofNullable(
                applicationMarketService.listMarketAppsByProjectId(projectId, pageRequest, searchParam))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.market.applications.get"));
    }

    /**
     * 查询发布级别为全局或者在本组织下的所有应用市场的应用
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @param searchParam 搜索参数
     * @return list of ApplicationReleasingDTO
     */
    @Permission(level = ResourceLevel.PROJECT)
    @ApiOperation(value = "查询发布级别为全局或者在本组织下的所有应用市场的应用")
    @CustomPageRequest
    @PostMapping(value = "/list_all")
    public ResponseEntity<Page<ApplicationReleasingDTO>> listAllApp(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable Long projectId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String searchParam) {
        return Optional.ofNullable(applicationMarketService.listMarketApps(projectId, pageRequest, searchParam))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.market.applications.get"));
    }

    /**
     * 查询单个应用市场的应用
     *
     * @param projectId   项目id
     * @param appMarketId 发布ID
     * @return list of ApplicationReleasingDTO
     */
    @Permission(level = ResourceLevel.PROJECT)
    @ApiOperation(value = "查询单个应用市场的应用")
    @CustomPageRequest
    @GetMapping("/{appMarketId}")
    public ResponseEntity<ApplicationReleasingDTO> queryApp(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable Long projectId,
            @ApiParam(value = "发布ID", required = true)
            @PathVariable Long appMarketId) {
        return Optional.ofNullable(applicationMarketService.getMarketApp(projectId, appMarketId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.market.application.get"));
    }
}
