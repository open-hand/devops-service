package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.api.vo.iam.entity.DevopsEnvFileE;

public interface DevopsEnvFileRepository {

    DevopsEnvFileE create(DevopsEnvFileE devopsEnvFileE);

    List<DevopsEnvFileE> listByEnvId(Long envId);

    DevopsEnvFileE queryByEnvAndPathAndCommit(Long envId, String path, String commit);

    DevopsEnvFileE queryByEnvAndPathAndCommits(Long envId, String path, List<String> commits);

    DevopsEnvFileE queryByEnvAndPath(Long envId, String path);

    void update(DevopsEnvFileE devopsEnvFileE);

    void delete(DevopsEnvFileE devopsEnvFileE);

    List<DevopsEnvFileE> listByEnvIdAndPath(Long envId, String path);
}
