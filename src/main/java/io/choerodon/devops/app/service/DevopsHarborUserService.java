package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.HarborUserDTO;

/**
 * @author: 25499
 * @date: 2019/10/23 11:53
 * @description:
 */
public interface DevopsHarborUserService {
    void baseCreateOrUpdate(HarborUserDTO harborUser);

    void baseCreate(HarborUserDTO harborUser);

    HarborUserDTO queryHarborUserById(Long id);

    void baseDelete(Long harborUserId);

}
