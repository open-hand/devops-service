package io.choerodon.devops.app.service;

import io.choerodon.devops.api.dto.DevopsAppResourceDTO;

import java.util.List;

/**
 * @author lizongwei
 * @date 2019/7/3
 */
public interface DevopsAppResourceService {

    void insert(DevopsAppResourceDTO devopsAppResourceDTO);

    void deleteByAppIdAndType(Long appId, String type);

    List<DevopsAppResourceDTO> queryByAppAndType(Long appId, String type);
}
