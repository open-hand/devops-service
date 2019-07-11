package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.infra.dto.DevopsMarketConnectInfoDO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  20:15 2019/7/2
 * Description:
 */
public interface MarketConnectInfoRepositpry {
    void createOrUpdate(DevopsMarketConnectInfoDO marketConnectInfoDO);

    DevopsMarketConnectInfoDO query();
}
