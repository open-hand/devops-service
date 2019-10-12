package io.choerodon.devops.api.controller.v1;

import java.util.Optional;
import java.util.Set;

import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.AppServiceVO;
import io.choerodon.devops.app.service.AppServiceService;

/**
 * @author zhaotianxin
 * @since 2019/9/20
 */
@RestController
@RequestMapping(value = "/v1/organizations/{organization_id}")
public class DevopsOrganizationController {
    @Autowired
    AppServiceService applicationServiceService;

    @Permission(type = ResourceType.SITE, permissionWithin = true)
    @ApiOperation(value = "批量查询应用服务")
    @PostMapping(value = "/app_service/list_app_service_ids")
    public ResponseEntity<PageInfo<AppServiceVO>> batchQueryAppService(
            @ApiParam(value = "组织ID")
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "应用服务Ids")
            @RequestParam(value = "ids") Set<Long> ids,
            @ApiParam(value = "是否分页")
            @RequestParam(value = "doPage", required = false) Boolean doPage,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return Optional.ofNullable(
                applicationServiceService.listAppServiceByIds(null,ids, doPage, pageRequest, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.list.app.service.ids"));
    }
}
