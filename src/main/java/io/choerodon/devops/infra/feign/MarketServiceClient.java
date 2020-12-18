package io.choerodon.devops.infra.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.devops.api.vo.market.MarketAppUseRecordDTO;
import io.choerodon.devops.api.vo.market.MarketServiceDeployObjectVO;
import io.choerodon.devops.api.vo.market.RepoConfigVO;
import io.choerodon.devops.infra.feign.fallback.MarketServiceClientFallback;

/**
 * Created by wangxiang on 2020/12/15
 */
@FeignClient(value = "market-service", fallback = MarketServiceClientFallback.class)
public interface MarketServiceClient {

    /**
     * 根据部署对象的id 查询部署对象的配置
     * @param projectId
     * @param deployObjectId
     * @return
     */
    @GetMapping("/v1/market/deploy/{project_id}/{deploy_object_id}/repo/config")
    ResponseEntity<MarketServiceDeployObjectVO> queryDeployObject(
            @PathVariable("project_id") Long projectId,
            @RequestParam("deploy_object_id") Long deployObjectId);

    /**
     * queryRepoConfig
     *
     * @param projectId
     * @param appId
     * @param appServiceVersionId
     * @return
     */
    @GetMapping("/v1/market/deploy/{project_id}/repo/config")
    ResponseEntity<RepoConfigVO> queryRepoConfig(
            @PathVariable("project_id") Long projectId,
            @RequestParam("app_service_id") Long appId,
            @RequestParam("app_service_version_id") Long appServiceVersionId);


    @PostMapping("/v1/application/use/record")
    ResponseEntity<Void> createUseRecord(
            @RequestBody MarketAppUseRecordDTO marketAppUseRecordDTO);
}
