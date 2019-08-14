package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.HarborMarketVO;
import io.choerodon.devops.app.eventhandler.payload.HarborPayload;

/**
 * Created with IntelliJ IDEA.
 * User: Runge
 * Date: 2018/4/8
 * Time: 10:30
 * Description:
 */
public interface HarborService {
    void createHarbor(HarborPayload harborPayload);

    void createHarborForAppMarket(HarborMarketVO harborMarketVO);

}
