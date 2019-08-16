package io.choerodon.devops.app.service;

import io.choerodon.devops.app.eventhandler.payload.ApplicationEventPayload;

/**
 * 与应用相关的逻辑
 *
 * @author zmf
 */
public interface ApplicationService {
    /**
     * 处理应用创建事件
     *
     * @param applicationEventPayload 应用信息
     */
    void handleApplicationCreation(ApplicationEventPayload applicationEventPayload);
}
