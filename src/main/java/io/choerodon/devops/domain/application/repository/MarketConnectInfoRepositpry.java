package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.infra.dataobject.DevopsMarketConnectInfoDO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  20:15 2019/7/2
 * Description:
 */
public interface MarketConnectInfoRepositpry {
    void create(DevopsMarketConnectInfoDO marketConnectInfoDO);

    void delete(String accessToken);

    DevopsMarketConnectInfoDO query();
}
