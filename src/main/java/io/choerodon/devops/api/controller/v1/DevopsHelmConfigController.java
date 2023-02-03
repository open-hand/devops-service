package io.choerodon.devops.api.controller.v1;

import java.util.List;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.DevopsHelmConfigVO;
import io.choerodon.devops.app.service.DevopsHelmConfigService;
import io.choerodon.swagger.annotation.Permission;

@RestController("DevopsHelmConfigController.v1")
@RequestMapping("/v1/projects/{project_id}/helm_config")
public class DevopsHelmConfigController {
    @Autowired
    private DevopsHelmConfigService helmConfigService;

    @ApiOperation("查询helm仓库列表")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/list")
    public ResponseEntity<List<DevopsHelmConfigVO>> listHelmConfig(
            @ApiParam("项目id")
            @PathVariable("project_id") Long projectId) {
        return Results.success(helmConfigService.listHelmConfig(projectId));
    }

    @ApiOperation("应用层查询helm仓库列表")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/app/list")
    public ResponseEntity<List<DevopsHelmConfigVO>> listHelmConfigOnApp(
            @ApiParam("项目id")
            @PathVariable("project_id") Long projectId,
            @ApiParam("项目id")
            @Encrypt
            @RequestParam(value = "app_service_id", required = false) Long appServiceId) {
        return Results.success(helmConfigService.listHelmConfigOnApp(projectId, appServiceId));
    }

    @ApiOperation("查询helm仓库")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{helm_config_id}")
    public ResponseEntity<DevopsHelmConfigVO> queryHelmConfig(
            @ApiParam("项目id")
            @PathVariable("project_id") Long projectId,
            @ApiParam("仓库id")
            @Encrypt @PathVariable("helm_config_id") Long helmConfigId) {
        return Results.success(helmConfigService.queryDevopsHelmConfig(projectId, helmConfigId));
    }

    @ApiOperation("检查helm名称是否已存在")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/check_name_exist")
    public ResponseEntity<Boolean> checkNameExist(@ApiParam("项目id")
                                                  @PathVariable("project_id") Long projectId,
                                                  @ApiParam("仓库名称")
                                                  @RequestParam("name") String name,
                                                  @ApiParam("仓库id")
                                                  @Encrypt @RequestParam(value = "helm_config_id", required = false) Long helmConfigId) {
        return Results.success(helmConfigService.checkNameExists(projectId, helmConfigId, name));
    }


    @ApiOperation("添加helm仓库")
    @PostMapping
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<DevopsHelmConfigVO> createDevopsHelmConfig(
            @ApiParam("项目id")
            @PathVariable("project_id") Long projectId,
            @RequestBody DevopsHelmConfigVO devopsHelmConfigVO) {
        return Results.success(helmConfigService.createDevopsHelmConfigOnProjectLevel(projectId, devopsHelmConfigVO));
    }

    @ApiOperation("更新helm仓库")
    @PutMapping
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<DevopsHelmConfigVO> updateDevopsHelmConfig(
            @ApiParam("项目id")
            @PathVariable("project_id") Long projectId,
            @RequestBody DevopsHelmConfigVO devopsHelmConfigVO) {
        return Results.success(helmConfigService.updateDevopsHelmConfigOnProjectLevel(projectId, devopsHelmConfigVO));
    }

    @ApiOperation("删除helm仓库")
    @DeleteMapping("/{helm_config_id}")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<Void> deleteHelmConfig(@ApiParam("项目id")
                                                 @PathVariable("project_id") Long projectId,
                                                 @ApiParam("仓库id")
                                                 @Encrypt @PathVariable("helm_config_id") Long helmConfigId) {
        helmConfigService.deleteDevopsHelmConfig(projectId, helmConfigId);
        return Results.success();
    }

    @ApiOperation("将指定仓库设为默认仓库")
    @PostMapping("/{helm_config_id}/set_default")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<Void> setDefaultHelmConfig(@ApiParam("项目id")
                                                     @PathVariable("project_id") Long projectId,
                                                     @ApiParam("仓库id")
                                                     @Encrypt @PathVariable("helm_config_id") Long helmConfigId) {
        helmConfigService.setDefaultDevopsHelmConfig(projectId, helmConfigId);
        return Results.success();
    }

    @ApiOperation("获取chart的index内容并返回给前端")
    @GetMapping("/{helm_config_id}/index")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<String> getIndexContent(@ApiParam("项目id")
                                                  @PathVariable("project_id") Long projectId,
                                                  @ApiParam("仓库id")
                                                  @Encrypt @PathVariable("helm_config_id") Long helmConfigId) {
        return Results.success(helmConfigService.getIndexContent(projectId, helmConfigId));
    }

    @ApiOperation("下载chart包")
    @GetMapping("/{helm_config_id}/charts/download")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<byte[]> downloadCharts(
            @ApiParam("项目id")
            @PathVariable("project_id") Long projectId,
            @ApiParam("仓库id")
            @Encrypt @PathVariable("helm_config_id") Long helmConfigId,
            @RequestParam("chart_url") String chartUrl) {
        String[] chartInfos = chartUrl.split("/");
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Disposition", String.format("attchement;filename=%s", chartInfos[1]));
        httpHeaders.add("content-type", "application/x-tar");
        return new ResponseEntity<>(helmConfigService.downloadChart(helmConfigId, chartUrl), httpHeaders, HttpStatus.OK);
    }
}