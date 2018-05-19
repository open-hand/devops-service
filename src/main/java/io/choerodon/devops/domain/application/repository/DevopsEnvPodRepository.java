package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.domain.application.entity.DevopsEnvPodE;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by Zenger on 2018/4/17.
 */
public interface DevopsEnvPodRepository {
    DevopsEnvPodE get(Long id);

    DevopsEnvPodE get(DevopsEnvPodE pod);

    void insert(DevopsEnvPodE devopsEnvPodE);

    List<DevopsEnvPodE> selectByInstanceId(Long instanceId);

    void update(DevopsEnvPodE devopsEnvPodE);

    Page<DevopsEnvPodE> listAppPod(Long projectId, PageRequest pageRequest, String searchParam);

    void deleteByName(String name, String namespace);
}
