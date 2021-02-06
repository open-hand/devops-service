package io.choerodon.devops.infra.feign.operator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.notify.MessageSettingVO;
import io.choerodon.devops.infra.feign.HzeroMessageClient;

/**
 * @author scp
 * @date 2020/12/7
 * @description
 */
@Component
public class HzeroMessageClientOperator {
    @Autowired
    private HzeroMessageClient hzeroMessageClient;

    public MessageSettingVO getMessageSettingVO(String notifyType, Long projectId, String messageCode) {
        MessageSettingVO messageSettingVO = hzeroMessageClient.queryByEnvIdAndEventNameAndProjectIdAndCode(notifyType, projectId, messageCode, null, null);
        if (messageSettingVO == null) {
            throw new CommonException("error.get.message.setting");
        }
        return messageSettingVO;
    }
}
