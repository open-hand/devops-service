package io.choerodon.devops.app.service;

import io.choerodon.devops.app.eventhandler.payload.HarborPayload;
import io.choerodon.devops.infra.feign.HarborClient;

/**
 * Created with IntelliJ IDEA.
 * User: Runge
 * Date: 2018/4/8
 * Time: 10:30
 * Description:
 */
public interface HarborService {

    void createHarborForProject(HarborPayload harborPayload);

    void createHarbor(HarborClient harborClient, String projectCode);
}
