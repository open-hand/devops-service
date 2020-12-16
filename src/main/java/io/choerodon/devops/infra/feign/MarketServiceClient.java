package io.choerodon.devops.infra.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import io.choerodon.devops.api.vo.market.RepoConfigVO;
import io.choerodon.devops.infra.feign.fallback.MarketServiceClientFallback;

/**
 * Created by wangxiang on 2020/12/15
 */
@FeignClient(value = "market-service", fallback = MarketServiceClientFallback.class)
public interface MarketServiceClient {


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
            @RequestParam("app_id") Long appId,
            @RequestParam("app_service_version_id") Long appServiceVersionId);
}
