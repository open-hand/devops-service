package io.choerodon.devops.api.controller.v1;

import java.util.List;

import io.swagger.annotations.ApiOperation;
import org.hzero.core.util.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.devops.api.vo.iam.ResourceVO;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.swagger.annotation.Permission;

/**
 * @Author: scp
 * @Description:
 * @Date: Created in 2021/5/7
 * @Modified By:
 */
@RestController
@RequestMapping("/v1/resource")
public class IamResourceController {

    @Autowired
    private AppServiceService appServiceService;

    @PostMapping("/list_by_ids")
    @ApiOperation(value = "查询项目下资源，内部接口，iam调用")
    @Permission(permissionWithin = true)
    public ResponseEntity<List<ResourceVO>> listResourceByIds(
            @RequestBody List<Long> projectIds) {
        return Results.success(appServiceService.listResourceByIds(projectIds));
    }

}
