package io.choerodon.devops.infra.feign.fallback;



import java.util.List;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.market.MarketAppUseRecordDTO;
import io.choerodon.devops.api.vo.market.MarketServiceDeployObjectVO;
import io.choerodon.devops.api.vo.market.MarketServiceVO;
import io.choerodon.devops.api.vo.market.RepoConfigVO;
import io.choerodon.devops.infra.dto.market.MarketChartValueDTO;
import io.choerodon.devops.infra.feign.MarketServiceClient;


@Component
public class MarketServiceClientFallback implements MarketServiceClient {

    @Override
    public ResponseEntity<MarketServiceDeployObjectVO> queryDeployObject(Long projectId, Long deployObjectId) {
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
    public ResponseEntity<MarketChartValueDTO> queryValuesForDeployObject(Long projectId, Long deployObjectId) {
        throw new CommonException("error.query.values.from.market");
    }

    @Override
    public ResponseEntity<MarketServiceVO> queryMarketService(Long projectId, Long marketServiceId) {
        throw new CommonException("error.query.market.service");
    }

    @Override
    public ResponseEntity<MarketServiceDeployObjectVO> queryDeployObjectByCodeAndService(Long projectId, String devopsAppServiceCode, String devopsAppServiceVersion, Boolean withHelmConfig) {
        throw new CommonException("error.query.deploy.object.by.code.and.service");
    }

    @Override
    public ResponseEntity<List<MarketServiceVO>> queryMarketServiceByIds(Long projectId, Set<Long> ids) {
        throw new CommonException("error.list.market.service.by.ids");
    }

    @Override
    public ResponseEntity<List<MarketServiceDeployObjectVO>> listDeployObjectsByIds(Long projectId, Set<Long> deployObjectIds) {
        throw new CommonException("error.list.deploy.objects.by.ids");
    }
}
