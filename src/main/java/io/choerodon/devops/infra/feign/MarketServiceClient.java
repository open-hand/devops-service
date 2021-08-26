package io.choerodon.devops.infra.feign;

import java.util.List;
import java.util.Set;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.market.MarketAppSubscribeRelVO;
import io.choerodon.devops.api.vo.market.MarketAppUseRecordDTO;
import io.choerodon.devops.api.vo.market.MarketServiceDeployObjectVO;
import io.choerodon.devops.api.vo.market.MarketServiceVO;
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
     *
     * @return {@link MarketServiceDeployObjectVO}
     */
    @GetMapping("/v1/projects/{project_id}/deploy/object/{deploy_object_id}/repo/config")
    ResponseEntity<String> queryDeployObject(
            @PathVariable("project_id") Long projectId,
            @Encrypt @RequestParam(value = "with_values") Boolean withValues,
            @Encrypt @PathVariable("deploy_object_id") Long deployObjectId);



    @PostMapping("/v1/application/use/record")
    ResponseEntity<Void> createUseRecord(
            @RequestBody MarketAppUseRecordDTO marketAppUseRecordDTO);

    /**
     * {@link MarketChartValueDTO}
     */
    @ApiOperation(value = "根据发布对象id查询values")
    @GetMapping("/v1/projects/{project_id}/deploy/values")
    ResponseEntity<String> queryValuesForDeployObject(
            @ApiParam("项目id")
            @PathVariable("project_id") Long projectId,
            @ApiParam("发布对象的id")
            @Encrypt @RequestParam("deploy_object_id") Long deployObjectId);

    /**
     * @return {@link MarketServiceVO}
     */
    @ApiOperation(value = "根据市场id查询市场服务本身的信息")
    @GetMapping("/v1/projects/{project_id}/market/service/{market_service_id}")
    ResponseEntity<String> queryMarketService(
            @PathVariable("project_id") Long projectId,
            @Encrypt @PathVariable("market_service_id") Long marketServiceId);

    /**
     * @return {@link MarketServiceDeployObjectVO}
     */
    @ApiOperation(value = "根据应用服务的code和版本 查询市场服务发布的部署对象（是否包含helm配置）/ (devops使用)")
    @GetMapping("/v1/projects/{project_id}/deploy/deploy_objects")
    @Permission(permissionWithin = true)
    ResponseEntity<String> queryDeployObjectByCodeAndService(
            @PathVariable("project_id") Long projectId,
            @RequestParam("devops_app_service_code") String devopsAppServiceCode,
            @RequestParam("devops_app_service_version") String devopsAppServiceVersion,
            @RequestParam(value = "with_helm_config", required = false, defaultValue = "false") Boolean withHelmConfig);

    /**
     * @return {@link List<MarketServiceVO>}
     */
    @ApiOperation(value = "根据多个市场服务ids查询市场服务的信息")
    @PostMapping("/v1/projects/{project_id}/market/service/list/by_ids")
    @Permission(permissionWithin = true)
    ResponseEntity<String> queryMarketServiceByIds(
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


    @ApiModelProperty(value = "增加应用订阅关系")
    @PostMapping("/v1/application/subscribe")
    ResponseEntity<MarketAppSubscribeRelVO> subscribeApplication(
            @Encrypt @RequestParam(name = "marketAppId") Long marketAppId,
            @Encrypt @RequestParam(name = "userId") Long userId);


    /**
     * {@link List<MarketServiceDeployObjectVO>}
     */
    @ApiOperation(value = "市场服务升级的时候查询可以升级的版本,当前部署版本的最新修复版本，和可部署的大版本(deploy_object_id为当前部署id)")
    @GetMapping("/v1/projects/{project_id}/deploy/application/version/upgrade/{market_service_id}")
    @Permission(level = ResourceLevel.ORGANIZATION)
    ResponseEntity<String> queryUpgradeMarketService(
            @PathVariable("project_id") Long projectId,
            @Encrypt @PathVariable("market_service_id") Long marketServiceId,
            @Encrypt @RequestParam("deploy_object_id") Long deployObjectId);

    /**
     * {@link MarketServiceDeployObjectVO}
     * 根据中间件名称(对应应用名称)、部署模式(对应市场服务名称)、版本(对应部署对象版本)查出对应的市场服务信息以及部署对象信息
     */
    @ApiOperation("根据中间件名称、部署模式、版本查处对应的市场服务信息以及部署对象信息")
    @Permission(permissionWithin = true)
    @GetMapping("/v1/middleware/service_release_info")
    ResponseEntity<String> getMiddlewareServiceReleaseInfo(
            @RequestParam("appName") String appName,
            @RequestParam("mode") String mode,
            @RequestParam("version") String version);


    @ApiOperation("根据marketServiceId 查询部署对象的列表")
    @GetMapping("/v1/projects/{project_id}/deploy/deploy_objects/by_market_service")
    ResponseEntity<String> queryDeployObjectByMarketServiceId(@PathVariable("project_id") Long projectId, @RequestParam("marketServiceId") Long marketServiceId);


    @ApiOperation("根据marketServiceIds 查询部署市场服务及其部署对象，所属的应用类型")
    @PostMapping("/v1/projects/{project_id}/market/service/deploy_and_category")
    ResponseEntity<String> queryMarketServiceAndDeployObjAndCategoryByMarketServiceId(
            @PathVariable("project_id") Long projectId,
            @RequestBody Set<Long> marketServiceIds);

    @GetMapping("/v1/market/application/{application_id}")
    @ApiOperation("查询应用详情")
    ResponseEntity<String> queryApplication(
            @PathVariable(name = "application_id", required = false) Long applicationId);

    @ApiOperation(value = "查询版本信息")
    @GetMapping("/v1/applications/{application_id}/app_versions/{app_version_id}")
    ResponseEntity<String> queryAppVersionById(
            @Encrypt @PathVariable(value = "application_id") Long applicationId,
            @Encrypt @PathVariable(value = "app_version_id") Long appVersionId);

    @ApiOperation(value = "查询hzero应用类型  （供部署使用）")
    @GetMapping("/v1/market/application/hzero/type")
    ResponseEntity<String> queryHzeroAppType(@RequestParam(value = "application_id") Long applicationId);
}