package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.dto.DevopsEnvFileErrorDTO;

/**
 * Creator: Runge
 * Date: 2018/8/10
 * Time: 11:03
 * Description:
 */
public interface DevopsEnvFileService {
    List<DevopsEnvFileErrorDTO> listByEnvId(Long envId);

    PageInfo<DevopsEnvFileErrorDTO> pageByEnvId(Long envId, PageRequest pageRequest);
}
