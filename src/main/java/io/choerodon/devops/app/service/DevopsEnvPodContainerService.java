package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.DevopsEnvPodContainerDTO;
import io.choerodon.devops.api.dto.DevopsEnvPodContainerLogDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Creator: Runge
 * Date: 2018/5/16
 * Time: 13:55
 * Description:
 */
public interface DevopsEnvPodContainerService {

    /**
     * 获取日志信息
     *
     * @param containerId 容器ID
     * @return DevopsEnvPodContainerLogDTO
     */
    DevopsEnvPodContainerLogDTO log(Long containerId);

    /**
     * 获取日志信息 By Pod
     *
     * @param podId pod ID
     * @return DevopsEnvPodContainerLogDTO
     */
    List<DevopsEnvPodContainerLogDTO> logByPodId(Long podId);

    /**
     * 分页查询容器
     *
     * @param podId       pod ID
     * @param pageRequest 分页参数
     * @param searchParam 查询参数
     * @return Page
     */
    Page<DevopsEnvPodContainerDTO> listByOptions(Long podId, PageRequest pageRequest, String searchParam);
}
