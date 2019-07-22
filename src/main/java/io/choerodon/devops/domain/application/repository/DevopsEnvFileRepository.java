package io.choerodon.devops.domain.application.repository;

import java.util.List;

public interface DevopsEnvFileRepository {

    DevopsEnvFileE baseCreate(DevopsEnvFileE devopsEnvFileE);

    List<DevopsEnvFileE> baseListByEnvId(Long envId);

    DevopsEnvFileE baseQueryByEnvAndPathAndCommit(Long envId, String path, String commit);

    DevopsEnvFileE baseQueryByEnvAndPathAndCommits(Long envId, String path, List<String> commits);

    DevopsEnvFileE baseQueryByEnvAndPath(Long envId, String path);

    void baseUpdate(DevopsEnvFileE devopsEnvFileE);

    void baseDelete(DevopsEnvFileE devopsEnvFileE);

    List<DevopsEnvFileE> baseListByEnvIdAndPath(Long envId, String path);
}
