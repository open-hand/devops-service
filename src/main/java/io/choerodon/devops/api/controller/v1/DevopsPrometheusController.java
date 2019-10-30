package io.choerodon.devops.api.controller.v1;

import java.util.Optional;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.PrometheusVo;
import io.choerodon.devops.app.service.DevopsPrometheusService;

/**
 * @author: 25499
 * @date: 2019/10/29 8:41
 * @description:
 */
@RestController
@RequestMapping(value = "/v1/clusters/{cluster_id}/prometheus")
public class DevopsPrometheusController {

    @Autowired
    private DevopsPrometheusService devopsPrometheusService;



}
