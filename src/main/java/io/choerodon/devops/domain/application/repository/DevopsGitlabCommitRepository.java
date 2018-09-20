package io.choerodon.devops.domain.application.repository;

import java.util.List;
import java.util.Map;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.CommitFormRecordDTO;
import io.choerodon.devops.domain.application.entity.DevopsGitlabCommitE;
import io.choerodon.devops.infra.dataobject.iam.UserDO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

public interface DevopsGitlabCommitRepository {

    DevopsGitlabCommitE create(DevopsGitlabCommitE devopsGitlabCommitE);

    DevopsGitlabCommitE queryBySha(String sha);

    List<DevopsGitlabCommitE> listCommitsByAppId(Long[] appIds);

    Page<CommitFormRecordDTO> pageCommitRecord(Long[] appId, PageRequest pageRequest, Map<Long, UserDO> userMap);

}
