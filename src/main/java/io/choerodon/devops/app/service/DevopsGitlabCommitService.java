package io.choerodon.devops.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.CommitFormRecordDTO;
import io.choerodon.devops.api.dto.DevopsGitlabCommitDTO;
import io.choerodon.devops.api.dto.PushWebHookDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

public interface DevopsGitlabCommitService {

    void create(PushWebHookDTO pushWebHookDTO, String token);

    DevopsGitlabCommitDTO getCommits(Long projectId, String[] appIds);

    Page<CommitFormRecordDTO> getRecordCommits(Long projectId,String[] appIds, PageRequest pageRequest);
}
