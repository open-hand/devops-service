package io.choerodon.devops.infra.feign.fallback;

import io.choerodon.core.notify.NoticeSendDTO;
import io.choerodon.devops.infra.feign.NotifyClient;

/**
 * Created by Sheep on 2019/5/15.
 */
public class NotifyServiceClientFallBack implements NotifyClient {
    @Override
    public void postEmail(NoticeSendDTO dto) {

    }
}
