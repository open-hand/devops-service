package io.choerodon.devops.domain.application.repository;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvPodE;

/**
 * Created by Zenger on 2018/4/17.
 */
public interface DevopsEnvPodRepository {
    DevopsEnvPodE baseQueryById(Long id);

    DevopsEnvPodE baseQueryByPod(DevopsEnvPodE pod);

    void baseCreate(DevopsEnvPodE devopsEnvPodE);

    List<DevopsEnvPodE> baseListByInstanceId(Long instanceId);

    void baseUpdate(DevopsEnvPodE devopsEnvPodE);

    PageInfo<DevopsEnvPodE> basePageByIds(Long projectId, Long envId, Long appId, Long instanceId, PageRequest pageRequest, String searchParam);

    void baseDeleteByName(String name, String namespace);

    DevopsEnvPodE queryByNameAndEnvName(String name, String namespace);
}
