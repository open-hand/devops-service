package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.MarketConnectInfoService;
import io.choerodon.devops.infra.dto.DevopsMarketConnectInfoDTO;
import io.choerodon.devops.infra.mapper.MarketConnectInfoMapper;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  15:02 2019/7/15
 * Description:
 */
public class MarketConnectInfoServiceImpl implements MarketConnectInfoService {
    @Autowired
    private MarketConnectInfoMapper marketConnectInfoMapper;

    @Override
    public void baseCreateOrUpdate(DevopsMarketConnectInfoDTO marketConnectInfoDO) {
        marketConnectInfoDO.setId(1L);
        if (marketConnectInfoMapper.selectByPrimaryKey(1L) == null) {
            if (marketConnectInfoMapper.insert(marketConnectInfoDO) != 1) {
                throw new CommonException("error.create.market.connect.info");
            }
        } else {
            if (marketConnectInfoMapper.updateByPrimaryKey(marketConnectInfoDO) != 1) {
                throw new CommonException("error.update.market.connect.info");
            }
        }
    }
    
    @Override
    public DevopsMarketConnectInfoDTO baseQuery() {
        return marketConnectInfoMapper.selectAll().get(0);
    }
}
