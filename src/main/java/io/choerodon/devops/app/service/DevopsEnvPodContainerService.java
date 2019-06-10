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
     * 获取日志信息 By Pod
     *
     * @param podId pod ID
     * @return DevopsEnvPodContainerLogDTO
     */
    List<DevopsEnvPodContainerLogDTO> logByPodId(Long podId);

}
