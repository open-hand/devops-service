package io.choerodon.devops.app.service;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.dto.DevopsEnvPodDTO;
import io.choerodon.devops.domain.application.entity.DevopsEnvPodE;

/**
 * Created by Zenger on 2018/4/17.
 */
public interface DevopsEnvPodService {

    /**
     * 分页查询容器管理
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @param searchParam 查询参数
     * @return page of devopsEnvPodDTO
     */
    PageInfo<DevopsEnvPodDTO> listAppPod(Long projectId, Long envId, Long appId, PageRequest pageRequest, String searchParam);

    void setContainers(DevopsEnvPodE devopsEnvPodE);

}
