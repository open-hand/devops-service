package io.choerodon.devops.api.controller.v1;

import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.hrdsCode.HarborC7nRepoImageTagVo;
import io.choerodon.devops.api.vo.hrdsCode.HarborC7nRepoVo;
import io.choerodon.devops.app.service.HarborService;
import io.choerodon.devops.infra.dto.harbor.HarborRepoConfigDTO;
import io.choerodon.devops.infra.feign.RdupmClient;
import io.choerodon.swagger.annotation.Permission;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @Autowired
    private RdupmClient rdupmClient;

    @GetMapping("/{projectId}/repo/list")
    @ApiOperation(value = "查询项目下所有的仓库配置")
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    public ResponseEntity<List<HarborRepoConfigDTO>> listAllCustomRepoByProject(@ApiParam(value = "猪齿鱼项目ID", required = true) @PathVariable("projectId") Long projectId) {
        List<HarborRepoConfigDTO> list = harborService.listAllCustomRepoByProject(projectId);
        return Results.success(list);
    }


    @ApiOperation(value = "根据项目ID获取镜像仓库列表")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/listImageRepo")
    public ResponseEntity<List<HarborC7nRepoVo>> listImageRepo(@ApiParam(value = "猪齿鱼项目ID", required = true)
                                                               @RequestParam("projectId") Long projectId) {
        return Results.success(rdupmClient.listImageRepo(projectId).getBody());
    }

    @ApiOperation(value = "根据仓库类型+仓库ID+镜像名称获取获取镜像版本")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/listImageTag")
    public ResponseEntity<HarborC7nRepoImageTagVo> listImageTag(@ApiParam(value = "仓库类型", required = true)
                                                                @RequestParam String repoType,
                                                                @ApiParam(value = "仓库ID", required = true)
                                                                @RequestParam Long repoId,
                                                                @ApiParam(value = "镜像名称", required = true)
                                                                @RequestParam String imageName,
                                                                @ApiParam(value = "镜像版本号,模糊查询")
                                                                @RequestParam(required = false) String tagName) {
        return Results.success(rdupmClient.listImageTag(repoType, repoId, imageName, tagName).getBody());
    }


}
