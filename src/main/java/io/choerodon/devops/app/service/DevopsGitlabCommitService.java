package io.choerodon.devops.app.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.CommitFormRecordDTO;
import io.choerodon.devops.api.vo.DevopsGitlabCommitVO;
import io.choerodon.devops.api.vo.PushWebHookDTO;
import io.choerodon.devops.api.vo.iam.entity.DevopsGitlabCommitE;
import io.choerodon.devops.api.vo.iam.entity.iam.UserE;
import io.choerodon.devops.infra.dto.DevopsGitlabCommitDTO;

public interface DevopsGitlabCommitService {

    void create(PushWebHookDTO pushWebHookDTO, String token);

    DevopsGitlabCommitVO getCommits(Long projectId, String appIds, Date startDate, Date endDate);

    PageInfo<CommitFormRecordDTO> getRecordCommits(Long projectId, String appIds, PageRequest pageRequest,
                                                   Date startDate, Date endDate);

    DevopsGitlabCommitDTO baseCreate(DevopsGitlabCommitDTO devopsGitlabCommitDTO);

    DevopsGitlabCommitDTO baseQueryByShaAndRef(String sha, String ref);

    List<DevopsGitlabCommitDTO> baseListByOptions(Long projectId, List<Long> appIds, Date startDate, Date endDate);

    PageInfo<CommitFormRecordDTO> basePageByOptions(Long projectId, List<Long> appId,
                                                    PageRequest pageRequest, Map<Long, UserE> userMap,
                                                    Date startDate, Date endDate);

    void baseUpdate(DevopsGitlabCommitDTO devopsGitlabCommitDTO);

    List<DevopsGitlabCommitDTO> baseListByAppIdAndBranch(Long appId, String branch, Date startDate);

}
