package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Optional;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.annotation.Permission;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.domain.Sort;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.ApplicationShareService;
import io.choerodon.mybatis.annotation.SortDefault;
import io.choerodon.swagger.annotation.CustomPageRequest;
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
@RequestMapping(value = "/v1/organizations/apps_share")
public class OrgAppShareController {
    @Autowired
    private ApplicationShareService applicationShareService;

    /**
     * 根据版本Id获取values和chart
     *
     * @param versionId 版本Id
     * @return Long
     */
    @Permission(type = ResourceType.SITE, permissionWithin = true)
    @ApiOperation(value = "根据版本Id获取values和chart")
    @GetMapping(value = "/values")
    public ResponseEntity<AppVersionAndValueVO> getValuesAndChart(
            @ApiParam(value = "应用Id")
            @RequestParam(value = "version_id") Long versionId) {
        return Optional.ofNullable(
                applicationShareService.getValuesAndChart(versionId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.get.values.chart"));
    }

    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "token校验")
    @PostMapping(value = "/check_token")
    public ResponseEntity<AccessTokenCheckResultVO> checkToken(
            @ApiParam(value = "token")
            @RequestBody AccessTokenVO tokenDTO) {
        return Optional.ofNullable(
                applicationShareService.checkToken(tokenDTO))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.check.access.token"));
    }

    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "token保存")
    @PostMapping(value = "/save_token")
    public ResponseEntity saveToken(
            @ApiParam(value = "token")
            @RequestBody AccessTokenVO tokenDTO) {
        applicationShareService.saveToken(tokenDTO);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}

