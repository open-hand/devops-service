package io.choerodon.devops.app.service;

import java.util.Date;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.CommitFormRecordDTO;
import io.choerodon.devops.api.dto.DevopsGitlabCommitDTO;
import io.choerodon.devops.api.dto.PushWebHookDTO;
import io.choerodon.devops.api.dto.TagHookDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

public interface DevopsGitlabCommitService {

    void create(PushWebHookDTO pushWebHookDTO, String token);

    DevopsGitlabCommitDTO getCommits(Long projectId, String appIds, Date startDate, Date endDate);

    Page<CommitFormRecordDTO> getRecordCommits(Long projectId, String appIds, PageRequest pageRequest,
                                               Date startDate, Date endDate);

    void createByTag(TagHookDTO tagHookDTO, String token);
}
