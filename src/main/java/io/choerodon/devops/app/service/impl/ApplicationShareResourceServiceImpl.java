package io.choerodon.devops.app.service.impl;

import java.util.List;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.ApplicationShareResourceService;
import io.choerodon.devops.infra.dto.ApplicationShareResourceDTO;
import io.choerodon.devops.infra.mapper.ApplicationShareResourceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Sheep on 2019/7/12.
 */

@Service
public class ApplicationShareResourceServiceImpl implements ApplicationShareResourceService {


    @Autowired
    private ApplicationShareResourceMapper applicationShareResourceMapper;


    public void baseCreate(ApplicationShareResourceDTO applicationShareResourceDTO) {
        if (applicationShareResourceMapper.insert(applicationShareResourceDTO) != 1) {
            throw new CommonException("error.insert.app.share.resource");
        }
    }

    public void baseDelete(Long shareId, Long projectId) {
        ApplicationShareResourceDTO applicationShareResourceDTO = new ApplicationShareResourceDTO();
        applicationShareResourceDTO.setShareId(shareId);
        applicationShareResourceDTO.setProjectId(projectId);
        applicationShareResourceMapper.deleteByPrimaryKey(applicationShareResourceDTO);
    }

    public List<ApplicationShareResourceDTO> baseListByShareId(Long shareId) {
        ApplicationShareResourceDTO applicationShareResourceDTO = new ApplicationShareResourceDTO();
        applicationShareResourceDTO.setShareId(shareId);
        return applicationShareResourceMapper.select(applicationShareResourceDTO);
    }

}
