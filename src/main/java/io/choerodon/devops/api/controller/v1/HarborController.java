package io.choerodon.devops.api.controller.v1;

import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.harbor.HarborCustomRepo;
import io.choerodon.devops.app.service.HarborService;
import io.choerodon.devops.infra.dto.harbor.HarborAllRepoDTO;
import io.choerodon.devops.infra.dto.harbor.HarborRepoConfigDTO;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * User: Mr.Wang
 * Date: 2020/6/11
 */
@RestController
@RequestMapping("/v1/harbor")
public class HarborController {

    @Autowired
    private HarborService harborService;

    @GetMapping("/{projectId}/repo/list")
    @ApiOperation(value = "查询项目下所有的仓库配置")
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    public ResponseEntity<List<HarborRepoConfigDTO>> listAllCustomRepoByProject(@ApiParam(value = "猪齿鱼项目ID", required = true) @PathVariable("projectId") Long projectId) {
        List<HarborRepoConfigDTO> list = harborService.listAllCustomRepoByProject(projectId);
        return Results.success(list);
    }

    //查询项目下仓库下的所有镜像


}
