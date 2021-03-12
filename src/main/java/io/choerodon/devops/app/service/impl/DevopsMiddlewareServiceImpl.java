package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.api.vo.AppServiceInstanceVO;
import io.choerodon.devops.api.vo.MarketInstanceCreationRequestVO;
import io.choerodon.devops.api.vo.MiddlewareRedisDeployVO;
import io.choerodon.devops.api.vo.market.MarketServiceDeployObjectVO;
import io.choerodon.devops.app.service.AppServiceInstanceService;
import io.choerodon.devops.app.service.DevopsMiddlewareService;
import io.choerodon.devops.infra.feign.operator.MarketServiceClientOperator;
import io.choerodon.devops.infra.util.ConvertUtils;

@Service
public class DevopsMiddlewareServiceImpl implements DevopsMiddlewareService {

    private static final String STAND_ALONE_CONFIG = "cluster:\n" +
            "  enabled: false\n";
    private static final String MIDDLEWARE_REDIS_NAME = "Redis";

    private static final String STANDALONE_MODE = "standalone";

    @Autowired
    private AppServiceInstanceService appServiceInstanceService;
    @Autowired
    private MarketServiceClientOperator marketServiceClientOperator;

    /**
     * 中间件的环境部署逻辑和市场应用的部署逻辑完全一样，只是需要提前构造values
     *
     * @param projectId
     * @param middlewareRedisDeployVO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppServiceInstanceVO deployForRedis(Long projectId, MiddlewareRedisDeployVO middlewareRedisDeployVO) {

        // 根据部署模式
        MarketServiceDeployObjectVO middlewareServiceReleaseInfo = marketServiceClientOperator.getMiddlewareServiceReleaseInfo(MIDDLEWARE_REDIS_NAME, middlewareRedisDeployVO.getMode(), middlewareRedisDeployVO.getVersion());

        middlewareRedisDeployVO.setMarketDeployObjectId(middlewareServiceReleaseInfo.getId());
        middlewareRedisDeployVO.setMarketAppServiceId(middlewareServiceReleaseInfo.getMarketServiceId());


        // 如果是单机模式，需要添加 禁用集群模式配置
        if (STANDALONE_MODE.equals(middlewareRedisDeployVO.getMode())) {
            middlewareRedisDeployVO.setValues(STAND_ALONE_CONFIG + middlewareRedisDeployVO.getValues());
        }

        MarketInstanceCreationRequestVO marketInstanceCreationRequestVO = ConvertUtils.convertObject(middlewareRedisDeployVO, MarketInstanceCreationRequestVO.class);

        return appServiceInstanceService.createOrUpdateMarketInstance(projectId, marketInstanceCreationRequestVO);
    }
}
