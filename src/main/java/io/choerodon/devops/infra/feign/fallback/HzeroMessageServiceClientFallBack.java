package io.choerodon.devops.infra.feign.fallback;

import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.notify.MessageSettingVO;
import io.choerodon.devops.infra.feign.HzeroMessageClient;

/**
 * Created by Sheep on 2019/5/15.
 */
@Component
public class HzeroMessageServiceClientFallBack implements HzeroMessageClient {

    @Override
    public MessageSettingVO queryByEnvIdAndEventNameAndProjectIdAndCode(String notifyType, Long projectId, String code, Long envId, String eventName) {
        throw new CommonException("error.query.message.setting");
    }
}
