package io.choerodon.devops.api.controller.v1;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.Permission;

import io.swagger.annotations.ApiOperation;
import org.hzero.core.base.BaseController;
import org.hzero.core.util.Results;
import org.hzero.mybatis.helper.SecurityTokenHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.devops.app.service.CiScheduleVariableService;

import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * devops_ci_schedule_variable(CiScheduleVariable)表控制层
 *
 * @author hao.wang08@hand-china.com
 * @since 2022-03-24 17:00:52
 */

@RestController("ciScheduleVariableController.v1")
@RequestMapping("/v1/{organizationId}/ci-schedule-variables")
public class CiScheduleVariableController extends BaseController {

    @Autowired
    private CiScheduleVariableService ciScheduleVariableService;

}

