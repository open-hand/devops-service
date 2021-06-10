package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.StatefulSetInfoVO;
import io.choerodon.devops.app.service.DevopsStatefulSetService;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.Permission;

@RestController
@RequestMapping(value = "/v1/projects/{project_id}/stateful_sets")
public class DevopsStatefulSetController {
    @Autowired
    private DevopsStatefulSetService devopsStatefulSetService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "分页查询statefulSets列表")
    @GetMapping("/paging")
    public ResponseEntity<Page<StatefulSetInfoVO>> pagingByEnvId(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @RequestParam(value = "env_id") @Encrypt Long envId,
            @ApiIgnore @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageable,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "from_instance", required = false) Boolean fromInstance
    ) {
        return ResponseEntity.ok(devopsStatefulSetService.pagingByEnvId(projectId, envId, pageable, name, fromInstance));
    }
}