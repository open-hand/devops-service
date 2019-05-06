package io.choerodon.devops.app.service;

import io.choerodon.devops.domain.application.event.HarborPayload;
import io.choerodon.devops.infra.dataobject.harbor.User;

/**
 * Created with IntelliJ IDEA.
 * User: Runge
 * Date: 2018/4/8
 * Time: 10:30
 * Description:
 */
public interface HarborService {
    void createHarbor(HarborPayload harborPayload);

}
