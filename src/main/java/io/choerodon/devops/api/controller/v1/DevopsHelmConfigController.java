package io.choerodon.devops.api.controller.v1;

import java.util.List;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.devops.api.vo.HelmConfigVO;
import io.choerodon.devops.app.service.DevopsHelmConfigService;

@RestController("DevopsHelmConfigController.v1")
@RequestMapping("/v1/projects/{project_id}/helm_config")
public class DevopsHelmConfigController {
    @Autowired
    private DevopsHelmConfigService helmConfigService;


    @ApiOperation("查询helm仓库")
    @GetMapping("/list")
    public ResponseEntity<List<HelmConfigVO>> listHelmConfig(
            @ApiParam("项目id")
            @PathVariable("project_id") Long projectId) {
        return Results.success(helmConfigService.listHelmConfig(projectId));
    }
}
