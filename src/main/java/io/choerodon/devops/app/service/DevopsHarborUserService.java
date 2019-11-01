package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.HarborUserDTO;

/**
 * @author: 25499
 * @date: 2019/10/23 11:53
 * @description:
 */
public interface DevopsHarborUserService {
    public void baseCreate(HarborUserDTO harborUser);

    public HarborUserDTO queryHarborUserById(Long id);

    public void baseDelete(Long harborUserId);

}
