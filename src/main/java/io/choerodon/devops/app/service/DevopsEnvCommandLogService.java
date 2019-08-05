package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.DevopsEnvCommandLogDTO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:48 2019/7/12
 * Description:
 */
public interface DevopsEnvCommandLogService {
    DevopsEnvCommandLogDTO baseCreate(DevopsEnvCommandLogDTO devopsEnvCommandLogDTO);

    DevopsEnvCommandLogDTO baseQuery(Long logId);

    List<DevopsEnvCommandLogDTO> baseListByDeployId(Long deployId);

    void baseDeleteByInstanceId(Long instanceId);

    void baseDeleteByCommandId(Long commandId);
}
