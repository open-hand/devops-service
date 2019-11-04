package io.choerodon.devops.app.service.impl;

import io.choerodon.devops.app.service.DevopsPvProPermissionService;
import io.choerodon.devops.infra.dto.DevopsPvProPermissionDTO;

import java.util.List;

public class DevopsPvProPermissionServiceImpl implements DevopsPvProPermissionService {

    @Override
    public void baseInsertPermission(DevopsPvProPermissionDTO devopsPvProPermissionDTO) {

    }

    @Override
    public void batchInsertIgnore(Long clusterId, List<Long> projectIds) {

    }

    @Override
    public List<DevopsPvProPermissionDTO> baseListByClusterId(Long clusterId) {
        return null;
    }

    @Override
    public void baseDeletePermission(DevopsPvProPermissionDTO devopsPvProPermissionDTO) {

    }

    @Override
    public void baseDeleteByPvId(Long pvId) {

    }
}
