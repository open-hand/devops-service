package io.choerodon.devops.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsPvProPermissionService;
import io.choerodon.devops.infra.dto.DevopsPvProPermissionDTO;
import io.choerodon.devops.infra.mapper.DevopsPvProPermissionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DevopsPvProPermissionServiceImpl implements DevopsPvProPermissionService {

    @Autowired
    DevopsPvProPermissionMapper devopsPvProPermissionMapper;

    @Override
    public void baseInsertPermission(DevopsPvProPermissionDTO devopsPvProPermissionDTO) {
        if (devopsPvProPermissionMapper.insert(devopsPvProPermissionDTO) != 1) {
            throw new CommonException("error.pv.project.permission.add");
        }
    }


    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void batchInsertIgnore(Long pvId, List<Long> projectIds) {
        if (projectIds == null){
            return;
        }

        DevopsPvProPermissionDTO devopsPvProPermissionDTO = new DevopsPvProPermissionDTO();
        devopsPvProPermissionDTO.setPvId(pvId);

        projectIds.forEach(projectId -> {
            devopsPvProPermissionDTO.setProjectId(projectId);
            if (devopsPvProPermissionMapper.selectOne(devopsPvProPermissionDTO) != null){
                devopsPvProPermissionMapper.insert(devopsPvProPermissionDTO);
            }
        });
    }

    @Override
    public List<DevopsPvProPermissionDTO> baseListByPvId(Long pvId) {
        DevopsPvProPermissionDTO devopsPvProPermissionDTO = new DevopsPvProPermissionDTO();
        devopsPvProPermissionDTO.setPvId(pvId);
        return devopsPvProPermissionMapper.select(devopsPvProPermissionDTO);
    }

    @Override
    public void baseDeletePermission(DevopsPvProPermissionDTO devopsPvProPermissionDTO) {
        devopsPvProPermissionMapper.delete(devopsPvProPermissionDTO);
    }


    @Override
    public void baseDeleteByPvId(Long pvId) {
        DevopsPvProPermissionDTO devopsPvProPermissionDTO = new DevopsPvProPermissionDTO();
        devopsPvProPermissionDTO.setPvId(pvId);
        devopsPvProPermissionMapper.delete(devopsPvProPermissionDTO);
    }
}
