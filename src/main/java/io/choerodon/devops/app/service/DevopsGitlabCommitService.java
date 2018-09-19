package io.choerodon.devops.app.service;

import io.choerodon.devops.api.dto.PushWebHookDTO;

public interface DevopsGitlabCommitService {

    void create(PushWebHookDTO pushWebHookDTO, String token);

}
