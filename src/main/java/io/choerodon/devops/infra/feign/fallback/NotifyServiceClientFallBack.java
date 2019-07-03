package io.choerodon.devops.infra.feign.fallback;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.notify.NoticeSendDTO;
import io.choerodon.devops.infra.feign.NotifyClient;
import org.springframework.stereotype.Component;

/**
 * Created by Sheep on 2019/5/15.
 */
@Component
public class NotifyServiceClientFallBack implements NotifyClient {
    @Override
    public void sendMessage(NoticeSendDTO dto) {
        throw new CommonException("error.message.send");
    }
}
