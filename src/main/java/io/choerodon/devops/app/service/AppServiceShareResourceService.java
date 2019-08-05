package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.AppServiceShareResourceDTO;

/**
 * Created by Sheep on 2019/7/12.
 */
public interface AppServiceShareResourceService {

    void baseCreate(AppServiceShareResourceDTO appServiceShareResourceDTO);

    List<AppServiceShareResourceDTO> baseListByShareId(Long shareId);

}
