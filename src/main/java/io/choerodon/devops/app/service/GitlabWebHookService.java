package io.choerodon.devops.app.service;

public interface GitlabWebHookService {

    void forwardingEventToPortal(String body, String token);

    void gitOpsWebHook(String body, String token);
}
