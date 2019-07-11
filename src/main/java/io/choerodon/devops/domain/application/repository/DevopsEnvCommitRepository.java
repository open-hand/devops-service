package io.choerodon.devops.domain.application.repository;


import java.util.List;

import io.choerodon.devops.api.vo.iam.entity.DevopsEnvCommitE;

public interface DevopsEnvCommitRepository {

    DevopsEnvCommitE create(DevopsEnvCommitE devopsEnvCommitE);

    DevopsEnvCommitE queryByEnvIdAndCommit(Long envId, String commit);

    DevopsEnvCommitE query(Long id);

    List<DevopsEnvCommitE> listByEnvId(Long envId);

}
