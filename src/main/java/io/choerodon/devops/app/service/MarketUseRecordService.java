package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.deploy.DeploySourceVO;

/**
 * Created by wangxiang on 2020/12/16
 */
public interface MarketUseRecordService {

    void saveMarketUseRecord(String purpose, Long projectId, DeploySourceVO deploySourceVO);
}
