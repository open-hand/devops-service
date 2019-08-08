package io.choerodon.devops.api.controller.v1;

import java.util.Optional;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.domain.Sort;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.AppServiceShareRuleService;
import io.choerodon.devops.app.service.OrgAppMarketService;
import io.choerodon.mybatis.annotation.SortDefault;
import io.choerodon.swagger.annotation.CustomPageRequest;

import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:30 2019/6/28
 * Description:
 */
@RestController
@RequestMapping(value = "/v1/organizations/app_market")
public class OrgAppMarketController {
    @Autowired
    private OrgAppMarketService orgAppMarketService;

    /**
     * @param appId
     * @param pageRequest
     * @param params
     * @return
     */
    @Permission(type = ResourceType.SITE, permissionWithin = true )
    @ApiOperation(value = "根据应用Id，获取应用服务和应用服务版本")
    @CustomPageRequest
    @PostMapping("/page_app_services")
    public ResponseEntity<PageInfo<AppServiceMarketVO>> pageByAppId(
            @ApiParam(value = "应用Id", required = true)
            @RequestParam(value = "app_id") Long appId,
            @ApiParam(value = "分页参数")
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest,
            @ApiParam(value = "查询参数",required = false)
            @RequestBody(required = false) String params) {
        return Optional.ofNullable(
                orgAppMarketService.pageByAppId(appId, pageRequest, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.app.services.page"));
    }

}

