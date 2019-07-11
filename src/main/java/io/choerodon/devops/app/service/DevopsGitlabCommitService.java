package io.choerodon.devops.app.service;

import java.util.Date;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.CommitFormRecordDTO;
import io.choerodon.devops.api.vo.DevopsGitlabCommitDTO;
import io.choerodon.devops.api.vo.PushWebHookDTO;

public interface DevopsGitlabCommitService {

    void create(PushWebHookDTO pushWebHookDTO, String token);

    DevopsGitlabCommitDTO getCommits(Long projectId, String appIds, Date startDate, Date endDate);

    PageInfo<CommitFormRecordDTO> getRecordCommits(Long projectId, String appIds, PageRequest pageRequest,
                                                   Date startDate, Date endDate);
}
