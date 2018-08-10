package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.DevopsEnvFileErrorDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Creator: Runge
 * Date: 2018/8/10
 * Time: 11:03
 * Description:
 */
public interface DevopsEnvFileService {
    List<DevopsEnvFileErrorDTO> listByEnvId(Long envId);

    Page<DevopsEnvFileErrorDTO> pageByEnvId(Long envId, PageRequest pageRequest);
}
