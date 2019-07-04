package io.choerodon.devops.infra.persistence.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.repository.MarketConnectInfoRepositpry;
import io.choerodon.devops.infra.dataobject.DevopsMarketConnectInfoDO;
import io.choerodon.devops.infra.mapper.MarketConnectInfoMapper;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  20:18 2019/7/2
 * Description:
 */
@Component
public class MarketConnectInfoRepositpryImpl implements MarketConnectInfoRepositpry {
    @Autowired
    private MarketConnectInfoMapper marketConnectInfoMapper;

    @Override
    public void createOrUpdate(DevopsMarketConnectInfoDO marketConnectInfoDO) {
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
    public DevopsMarketConnectInfoDO query() {
        return marketConnectInfoMapper.selectAll().get(0);
    }
}
