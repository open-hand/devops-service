package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.ApplicationShareResourceDTO;

/**
 * Created by Sheep on 2019/7/12.
 */
public interface ApplicationShareResourceService {

    void baseCreate(ApplicationShareResourceDTO applicationShareResourceDTO);

    List<ApplicationShareResourceDTO> baseListByShareId(Long shareId);

}
