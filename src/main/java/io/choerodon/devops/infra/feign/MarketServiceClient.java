package io.choerodon.devops.infra.feign;

import java.util.List;
import java.util.Set;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.devops.api.vo.market.MarketAppUseRecordDTO;
import io.choerodon.devops.api.vo.market.MarketServiceDeployObjectVO;
import io.choerodon.devops.api.vo.market.MarketServiceVO;
import io.choerodon.devops.api.vo.market.RepoConfigVO;
import io.choerodon.devops.infra.dto.market.MarketChartValueDTO;
import io.choerodon.devops.infra.feign.fallback.MarketServiceClientFallback;
import io.choerodon.swagger.annotation.Permission;

/**
 * Created by wangxiang on 2020/12/15
 */
@FeignClient(value = "market-service", fallback = MarketServiceClientFallback.class)
public interface MarketServiceClient {

    /**
     * 根据部署对象的id 查询部署对象的配置
     */
    @GetMapping("/v1/projects/{project_id}/deploy/object/{deploy_object_id}/repo/config")
    ResponseEntity<MarketServiceDeployObjectVO> queryDeployObject(
            @PathVariable("project_id") Long projectId,
            @Encrypt @PathVariable("deploy_object_id") Long deployObjectId);

//    /**
//     * queryRepoConfig
//     */
//    @GetMapping("/v1/projects/{project_id}/deploy/{project_id}/repo/config")
//    ResponseEntity<RepoConfigVO> queryRepoConfig(
//            @PathVariable("project_id") Long projectId,
//            @Encrypt @RequestParam("app_service_id") Long appId,
//            @Encrypt @RequestParam("app_service_version_id") Long appServiceVersionId);


    @PostMapping("/v1/application/use/record")
    ResponseEntity<Void> createUseRecord(
            @RequestBody MarketAppUseRecordDTO marketAppUseRecordDTO);

    @ApiOperation(value = "根据发布对象id查询values")
    @GetMapping("/v1/projects/{project_id}/deploy/values")
    ResponseEntity<MarketChartValueDTO> queryValuesForDeployObject(
            @ApiParam("项目id")
            @PathVariable("project_id") Long projectId,
            @ApiParam("发布对象的id")
            @Encrypt @RequestParam("deploy_object_id") Long deployObjectId);

    @ApiOperation(value = "根据市场id查询市场服务本身的信息")
    @GetMapping("/v1/projects/{project_id}/market/service/{market_service_id}")
    ResponseEntity<MarketServiceVO> queryMarketService(
            @PathVariable("project_id") Long projectId,
            @Encrypt @PathVariable("market_service_id") Long marketServiceId);

    @ApiOperation(value = "根据应用服务的code和版本 查询市场服务发布的部署对象（是否包含helm配置）/ (devops使用)")
    @GetMapping("/v1/projects/{project_id}/deploy/deploy_objects")
    @Permission(permissionWithin = true)
    ResponseEntity<MarketServiceDeployObjectVO> queryDeployObjectByCodeAndService(
            @PathVariable("project_id") Long projectId,
            @RequestParam("devops_app_service_code") String devopsAppServiceCode,
            @RequestParam("devops_app_service_version") String devopsAppServiceVersion,
            @RequestParam(value = "with_helm_config", required = false, defaultValue = "false") Boolean withHelmConfig);

    @ApiOperation(value = "根据多个市场服务ids查询市场服务的信息")
    @PostMapping("/v1/projects/{project_id}/market/service/list/by_ids")
    @Permission(permissionWithin = true)
    ResponseEntity<List<MarketServiceVO>> queryMarketServiceByIds(
            @PathVariable("project_id") Long projectId,
            @RequestBody Set<Long> ids);

    /**
     * @param projectId       项目id
     * @param deployObjectIds 部署对象id
     * @return {@link List<MarketServiceDeployObjectVO>}
     */
    @ApiOperation(value = "根据多个发布对象id查询发布对象基础信息/不包含级联信息")
    @PostMapping("/v1/projects/{project_id}/deploy/deploy_objects/list/by_ids")
    ResponseEntity<String> listDeployObjectsByIds(
            @ApiParam("项目id")
            @PathVariable("project_id") Long projectId,
            @ApiParam("发布对象id集合")
            @RequestBody Set<Long> deployObjectIds);
}