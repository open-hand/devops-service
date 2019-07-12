package io.choerodon.devops.domain.application.repository;


import java.util.List;

import io.choerodon.devops.api.vo.iam.entity.DevopsEnvCommitVO;

public interface DevopsEnvCommitRepository {

    DevopsEnvCommitVO baseCreate(DevopsEnvCommitVO devopsEnvCommitE);

    DevopsEnvCommitVO baseQueryByEnvIdAndCommit(Long envId, String commit);

    DevopsEnvCommitVO baseQuery(Long id);

    List<DevopsEnvCommitVO> baseListByEnvId(Long envId);

}
