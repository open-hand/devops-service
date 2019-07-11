package io.choerodon.devops.app.service;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.DevopsEnvPodDTO;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvPodE;

/**
 * Created by Zenger on 2018/4/17.
 */
public interface DevopsEnvPodService {

    /**
     *
     * @param projectId
     * @param envId
     * @param appId
     * @param instanceId
     * @param pageRequest
     * @param searchParam
     * @return
     */
    PageInfo<DevopsEnvPodDTO> listAppPod(Long projectId, Long envId, Long appId, Long instanceId, PageRequest pageRequest, String searchParam);

    void setContainers(DevopsEnvPodE devopsEnvPodE);

}
