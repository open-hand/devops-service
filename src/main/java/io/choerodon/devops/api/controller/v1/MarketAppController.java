package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.AppServiceImportVO;
import io.choerodon.devops.api.vo.ApplicationImportInternalVO;
import io.choerodon.devops.api.vo.MarketApplicationImportVO;
import io.choerodon.devops.app.service.MarketAppService;
import io.choerodon.swagger.annotation.Permission;

/**
 * Created by wangxiang on 2021/3/2
 */
@RestController
@RequestMapping("/v1/project/{project_id}/market/app")
public class MarketAppController {


    @Autowired
    private MarketAppService marketAppService;


    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "导入市场服务的应用服务")
    @PostMapping("/import")
    public ResponseEntity<Void> importAppService(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "服务信息", required = true)
            @RequestBody List<ApplicationImportInternalVO> applicationImportInternalVOS) {
        marketAppService.importAppService(projectId, applicationImportInternalVOS);
        return ResponseEntity.noContent().build();
    }
}
