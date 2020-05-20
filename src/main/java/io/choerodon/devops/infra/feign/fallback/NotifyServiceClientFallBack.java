package io.choerodon.devops.infra.feign.fallback;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.notify.NoticeSendDTO;
import io.choerodon.devops.api.vo.notify.MessageSettingVO;
import io.choerodon.devops.api.vo.notify.SendSettingDTO;
import io.choerodon.devops.infra.feign.NotifyClient;
import org.springframework.http.ResponseEntity;
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

    @Override
    public ResponseEntity<SendSettingDTO> queryByCode(String code) {
        throw new CommonException("error.query.setting");
    }

    @Override
    public MessageSettingVO queryByEnvIdAndEventNameAndProjectIdAndCode(String notifyType, Long projectId, String code, Long envId, String eventName) {
        throw new CommonException("error.query.message.setting");
    }
}
