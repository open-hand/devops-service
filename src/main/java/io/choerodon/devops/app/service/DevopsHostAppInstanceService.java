package io.choerodon.devops.app.service;

import java.util.List;

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

}
