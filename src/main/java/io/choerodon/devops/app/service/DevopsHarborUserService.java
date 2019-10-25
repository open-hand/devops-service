package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.HarborUserDTO;
import io.choerodon.devops.infra.dto.harbor.User;

/**
 * @author: 25499
 * @date: 2019/10/23 11:53
 * @description:
 */
public interface DevopsHarborUserService {
    public long create(HarborUserDTO harborUser);

    public HarborUserDTO queryHarborUserById(Long id);

}
