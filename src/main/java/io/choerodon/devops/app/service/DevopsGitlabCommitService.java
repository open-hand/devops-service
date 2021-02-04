package io.choerodon.devops.app.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.CommitFormRecordVO;
import io.choerodon.devops.api.vo.DevopsGitlabCommitVO;
import io.choerodon.devops.api.vo.PushWebHookVO;
import io.choerodon.devops.infra.dto.DevopsGitlabCommitDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

public interface DevopsGitlabCommitService {

    void create(PushWebHookVO pushWebHookVO, String token);

    DevopsGitlabCommitVO queryCommits(Long projectId, String appServiceIds, Date startDate, Date endDate);

    Page<CommitFormRecordVO> pageRecordCommits(Long projectId, String appServiceIds, PageRequest pageable,
                                               Date startDate, Date endDate);

    Page<CommitFormRecordVO> pageIndividualCommits(Long projectId, Date startDate, Date endDate, PageRequest pageable);

    DevopsGitlabCommitDTO baseCreate(DevopsGitlabCommitDTO devopsGitlabCommitDTO);

    DevopsGitlabCommitDTO baseQueryByShaAndRef(String sha, String ref);

    List<DevopsGitlabCommitDTO> baseListByOptions(Long projectId, List<Long> appServiceIds, Date startDate, Date endDate);

    Page<CommitFormRecordVO> basePageByOptions(Long projectId, List<Long> appServiceId,
                                               PageRequest pageable, Map<Long, IamUserDTO> userMap,
                                               Date startDate, Date endDate, Long userId);

    void baseUpdate(DevopsGitlabCommitDTO devopsGitlabCommitDTO);

    List<DevopsGitlabCommitDTO> baseListByAppIdAndBranch(Long appServiceId, String branch, Date startDate);

}
