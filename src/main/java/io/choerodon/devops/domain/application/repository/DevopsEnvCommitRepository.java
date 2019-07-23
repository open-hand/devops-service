package io.choerodon.devops.domain.application.repository;


import java.util.List;

public interface DevopsEnvCommitRepository {

    DevopsEnvCommitVO baseCreate(DevopsEnvCommitVO devopsEnvCommitE);

    DevopsEnvCommitVO baseQueryByEnvIdAndCommit(Long envId, String commit);

    DevopsEnvCommitVO baseQuery(Long id);

    List<DevopsEnvCommitVO> baseListByEnvId(Long envId);

}
