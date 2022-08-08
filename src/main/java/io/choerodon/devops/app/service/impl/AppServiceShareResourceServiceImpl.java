package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.AppServiceShareResourceService;
import io.choerodon.devops.infra.dto.AppServiceShareResourceDTO;
import io.choerodon.devops.infra.mapper.AppShareResourceMapper;

/**
 * Created by Sheep on 2019/7/12.
 */

@Service
public class AppServiceShareResourceServiceImpl implements AppServiceShareResourceService {


    @Autowired
    private AppShareResourceMapper applicationShareResourceMapper;

    @Override
    public void baseCreate(AppServiceShareResourceDTO appServiceShareResourceDTO) {
        if (applicationShareResourceMapper.insert(appServiceShareResourceDTO) != 1) {
            throw new CommonException("error.insert.app.share.resource");
        }
    }

    public void baseDelete(Long shareId, Long projectId) {
        AppServiceShareResourceDTO appServiceShareResourceDTO = new AppServiceShareResourceDTO();
        appServiceShareResourceDTO.setShareId(shareId);
        appServiceShareResourceDTO.setProjectId(projectId);
        applicationShareResourceMapper.deleteByPrimaryKey(appServiceShareResourceDTO);
    }

}
