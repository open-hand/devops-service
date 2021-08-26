package io.choerodon.devops.api.controller.v1;

import org.hzero.core.util.Results;
import org.hzero.core.base.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.hzero.mybatis.helper.SecurityTokenHelper;
import io.choerodon.devops.app.service.DeployConfigService;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;
import io.swagger.annotations.ApiParam;

/**
 * 主机部署文件配置表 管理 API
 *
 * @author jian.zhang02@hand-china.com 2021-08-19 15:43:01
 */
@RestController("deployConfigController.v1")
@RequestMapping("/v1/{organizationId}/deploy-configs")
public class DeployConfigController extends BaseController {

    @Autowired
    private DeployConfigService deployConfigService;

}
