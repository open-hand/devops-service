package io.choerodon.devops.domain.application.repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.CommitFormRecordVO;
import io.choerodon.devops.api.vo.iam.entity.iam.UserE;

public interface DevopsGitlabCommitRepository {

    DevopsGitlabCommitE baseCreate(DevopsGitlabCommitE devopsGitlabCommitE);

    DevopsGitlabCommitE baseQueryByShaAndRef(String sha, String ref);

    List<DevopsGitlabCommitE> baseListByOptions(Long projectId, List<Long> appIds, Date startDate, Date endDate);

    PageInfo<CommitFormRecordVO> basePageByOptions(Long projectId, List<Long> appId,
                                                   PageRequest pageRequest, Map<Long, UserE> userMap,
                                                   Date startDate, Date endDate);

    void baseUpdate(DevopsGitlabCommitE devopsGitlabCommitE);

    List<DevopsGitlabCommitE> baseListByAppIdAndBranch(Long appId, String branch, Date startDate);

}
