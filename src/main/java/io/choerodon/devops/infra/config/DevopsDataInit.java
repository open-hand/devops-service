package io.choerodon.devops.infra.config;

import org.hzero.core.message.MessageAccessor;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

@Component
public class DevopsDataInit implements SmartInitializingSingleton {

    @Override
    public void afterSingletonsInstantiated() {
        MessageAccessor.addBasenames("classpath:messages/messages");
    }
}
