package io.choerodon.devops.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.DevopsEnvPodDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

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
    Page<DevopsEnvPodDTO> listAppPod(Long projectId, PageRequest pageRequest, String searchParam);
}
