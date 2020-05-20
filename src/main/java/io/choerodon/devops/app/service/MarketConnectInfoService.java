package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.DevopsMarketConnectInfoDTO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:56 2019/7/15
 * Description:
 */
public interface MarketConnectInfoService {
    void baseCreateOrUpdate(DevopsMarketConnectInfoDTO marketConnectInfoDTO);

    DevopsMarketConnectInfoDTO baseQuery();
}
