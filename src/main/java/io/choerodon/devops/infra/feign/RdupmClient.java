package io.choerodon.devops.infra.feign;


import io.choerodon.devops.api.vo.harbor.HarborCustomRepoVO;
import io.choerodon.devops.infra.feign.fallback.RdupmClientFallback;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User: Mr.Wang
 * Date: 2020/6/11
 */
@FeignClient(value = "hrds-prod-repo", fallback = RdupmClientFallback.class)
public interface RdupmClient {

    /**
     * 查询项目下所有自定义仓库
     *
     * @param projectId
     * @return
     */
    @GetMapping("/v1/harbor-choerodon-repos/project/{projectId}/list_all_custom_repo")
    ResponseEntity<List<HarborCustomRepoVO>> listAllCustomRepoByProject(@PathVariable("projectId") Long projectId);


    /**
     * 查询关联的自定义仓库
     *
     * @param projectId
     * @param appServiceId
     * @return
     */
    @GetMapping("/v1/harbor-choerodon-repos/project/{projectId}/{appServiceId}/list_related_custom_repo")
    ResponseEntity<HarborCustomRepoVO> listRelatedCustomRepoByService(@ApiParam(value = "猪齿鱼项目ID", required = true)
                                                                      @PathVariable("projectId") Long projectId,
                                                                      @ApiParam(value = "应用服务ID", required = true)
                                                                      @PathVariable("appServiceId") Long appServiceId);

    /**
     * 保存关联关系
     *
     * @param projectId
     * @param appServiceId
     * @param customRepoId
     * @return
     */
    @PostMapping("/v1/harbor-choerodon-repos/project/{projectId}/{appServiceId}/save_relation")
    ResponseEntity saveRelationByService(@ApiParam(value = "猪齿鱼项目ID", required = true)
                                         @PathVariable("projectId") Long projectId,
                                         @ApiParam(value = "应用服务ID", required = true)
                                         @PathVariable("appServiceId") Long appServiceId,
                                         @ApiParam(value = "自定义仓库ID", required = true) @RequestParam Long customRepoId);


    /**
     * 删除关联关系
     *
     * @param projectId
     * @param appServiceId
     * @param customRepoId
     * @return
     */
    @DeleteMapping("/v1/harbor-choerodon-repos/project/{projectId}/{appServiceId}/delete_relation")
    ResponseEntity deleteRelationByService(@ApiParam(value = "猪齿鱼项目ID", required = true)
                                           @PathVariable("projectId") Long projectId,
                                           @ApiParam(value = "应用服务ID", required = true)
                                           @PathVariable("appServiceId") Long appServiceId,
                                           @ApiParam(value = "自定义仓库ID", required = true)
                                           @RequestParam Long customRepoId);

    /**
     * 仓库配置查询接口
     * @param projectId
     * @param appServiceId
     * @return
     */
//    @GetMapping("/v1/harbor-choerodon-repos/project/{projectId}/{appServiceId}/harbor_repo_config")
//    public ResponseEntity<HarborRepoDTO> queryHarborRepoConfig(@ApiParam(value = "猪齿鱼项目ID", required = true)
//                                                               @PathVariable("projectId") Long projectId,
//                                                               @ApiParam(value = "应用服务ID", required = true)
//                                                               @PathVariable("appServiceId") Long appServiceId);


}
