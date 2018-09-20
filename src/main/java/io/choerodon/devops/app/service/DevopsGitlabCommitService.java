package io.choerodon.devops.app.service;

import io.choerodon.devops.api.dto.DevopsGitlabCommitDTO;
import io.choerodon.devops.api.dto.PushWebHookDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

public interface DevopsGitlabCommitService {

    void create(PushWebHookDTO pushWebHookDTO, String token);

    DevopsGitlabCommitDTO getCommits(Long[] applicationId, PageRequest pageRequest);
}
