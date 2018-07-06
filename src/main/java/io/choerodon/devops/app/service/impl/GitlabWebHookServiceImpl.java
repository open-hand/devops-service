package io.choerodon.devops.app.service.impl;

import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.GitlabWebHookService;

@Service
public class GitlabWebHookServiceImpl implements GitlabWebHookService {
    @Override
    public void forwardingEventToPortal(String body, String token) {

    }
}
