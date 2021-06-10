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
import io.choerodon.devops.api.vo.JobInfoVO;
import io.choerodon.devops.app.service.DevopsJobService;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.Permission;

@RestController
@RequestMapping(value = "/v1/projects/{project_id}/jobs")
public class DevopsJobController {

    @Autowired
    private DevopsJobService devopsJobService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "分页查询deployment列表")
    @GetMapping("/paging")
    public ResponseEntity<Page<JobInfoVO>> pagingByEnvId(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @RequestParam(value = "env_id") @Encrypt Long envId,
            @ApiIgnore @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageable,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "from_instance", required = false) Boolean fromInstance
    ) {
        return ResponseEntity.ok(devopsJobService.pagingByEnvId(projectId, envId, pageable, name, fromInstance));
    }
}
