package io.choerodon.devops.domain.application.repository;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.domain.application.entity.DevopsEnvPodE;

/**
 * Created by Zenger on 2018/4/17.
 */
public interface DevopsEnvPodRepository {
    DevopsEnvPodE get(Long id);

    DevopsEnvPodE get(DevopsEnvPodE pod);

    void insert(DevopsEnvPodE devopsEnvPodE);

    List<DevopsEnvPodE> selectByInstanceId(Long instanceId);

    void update(DevopsEnvPodE devopsEnvPodE);

    PageInfo<DevopsEnvPodE> listAppPod(Long projectId, Long envId, Long appId, Long instanceId, PageRequest pageRequest, String searchParam);

    void deleteByName(String name, String namespace);

    DevopsEnvPodE getByNameAndEnv(String name, String namespace);
}
