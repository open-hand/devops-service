package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Set;

import io.choerodon.devops.infra.dto.DevopsHostAppInstanceDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/9/3 15:51
 */
public interface DevopsHostAppInstanceService {

    void baseCreate(DevopsHostAppInstanceDTO devopsHostAppInstanceDTO);

    List<DevopsHostAppInstanceDTO> listByAppId(Long appId);

    void baseUpdate(DevopsHostAppInstanceDTO devopsHostAppInstanceDTO);

    void updateKillCommand(Long id, String killCommand);

    void updateHealthProb(Long id, String healthProb);

    List<DevopsHostAppInstanceDTO> listByHostId(Long hostId);

    void baseDelete(Long id);

    DevopsHostAppInstanceDTO baseQuery(Long id);

    List<DevopsHostAppInstanceDTO> listByAppIds(Set<Long> appIds);
}
