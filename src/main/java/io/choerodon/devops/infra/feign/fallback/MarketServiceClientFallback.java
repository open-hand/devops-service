package io.choerodon.devops.infra.feign.fallback;


import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.market.MarketAppSubscribeRelVO;
import io.choerodon.devops.api.vo.market.MarketAppUseRecordDTO;
import io.choerodon.devops.infra.feign.MarketServiceClient;


@Component
public class MarketServiceClientFallback implements MarketServiceClient {

    @Override
    public ResponseEntity<String> queryDeployObject(Long projectId, Boolean withValues, Long deployObjectId) {
        throw new CommonException("error.query.deploy.object.config");
    }

//    @Override
//    public ResponseEntity<RepoConfigVO> queryRepoConfig(Long projectId, Long appId, Long appServiceVersionId) {
//        throw new CommonException("error.query.repo.config");
//    }

    @Override
    public ResponseEntity<Void> createUseRecord(MarketAppUseRecordDTO marketAppUseRecordDTO) {
        throw new CommonException("error.create.use.record");
    }

    @Override
    public ResponseEntity<String> queryValuesForDeployObject(Long projectId, Long deployObjectId) {
        throw new CommonException("error.query.values.from.market");
    }

    @Override
    public ResponseEntity<String> queryMarketService(Long projectId, Long marketServiceId) {
        throw new CommonException("error.query.market.service");
    }

    @Override
    public ResponseEntity<String> queryDeployObjectByCodeAndService(Long projectId, String devopsAppServiceCode, String devopsAppServiceVersion, Boolean withHelmConfig) {
        throw new CommonException("error.query.deploy.object.by.code.and.service");
    }

    @Override
    public ResponseEntity<String> queryMarketServiceByIds(Long projectId, Set<Long> ids) {
        throw new CommonException("error.list.market.service.by.ids");
    }

    @Override
    public ResponseEntity<String> listDeployObjectsByIds(Long projectId, Set<Long> deployObjectIds) {
        throw new CommonException("error.list.deploy.objects.by.ids");
    }

    @Override
    public ResponseEntity<MarketAppSubscribeRelVO> subscribeApplication(Long marketAppId, Long userId) {
        throw new CommonException("error.subscribe.application");
    }

    @Override
    public ResponseEntity<String> queryUpgradeMarketService(Long projectId, Long marketServiceId, Long deployObjectId) {
        throw new CommonException("error.query.market.upgrade.version");
    }

    @Override
    public ResponseEntity<String> getMiddlewareServiceReleaseInfo(String appName, String mode, String version) {
        throw new CommonException("error.query.middleware.info");
    }

    @Override
    public ResponseEntity<String> queryDeployObjectByMarketServiceId(Long projectId, Long marketServiceId) {
        throw new CommonException("error.query.deploy.objects.info");
    }

    @Override
    public ResponseEntity<String> queryMarketServiceAndDeployObjAndCategoryByMarketServiceId(Long projectId, Set<Long> marketServiceIds) {
        throw new CommonException("error.query.market.service.info");
    }

    @Override
    public ResponseEntity<String> queryApplication(Long applicationId) {
        throw new CommonException("error.query.market.app");
    }

    @Override
    public ResponseEntity<String> queryAppVersionById(Long applicationId, Long appVersionId) {
        throw new CommonException("error.query.market.app.version");
    }

    @Override
    public ResponseEntity<String> queryHzeroAppType(Long applicationId) {
        throw new CommonException("error.query.hzero.app.type");
    }
}
