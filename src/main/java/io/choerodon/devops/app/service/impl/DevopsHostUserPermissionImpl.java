package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.hzero.mybatis.BatchInsertHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsHostUserPermissionService;
import io.choerodon.devops.infra.dto.DevopsHostUserPermissionDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsHostUserPermissionMapper;

@Service
public class DevopsHostUserPermissionImpl implements DevopsHostUserPermissionService {
    @Autowired
    private DevopsHostUserPermissionMapper devopsHostUserPermissionMapper;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;

    @Autowired
    @Qualifier("devopsHostUserPermissionInsertHelper")
    private BatchInsertHelper<DevopsHostUserPermissionDTO> batchInsertHelper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(DevopsHostUserPermissionDTO devopsHostUserPermissionDTO) {
        if (devopsHostUserPermissionMapper.insert(devopsHostUserPermissionDTO) != 1) {
            throw new CommonException("error.insert.host.user.permission");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchInsert(List<DevopsHostUserPermissionDTO> devopsHostUserPermissionDTOList) {
        batchInsertHelper.batchInsert(devopsHostUserPermissionDTOList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByHostId(Long hostId) {
        DevopsHostUserPermissionDTO devopsHostUserPermissionDTO = new DevopsHostUserPermissionDTO();
        devopsHostUserPermissionDTO.setHostId(hostId);
        devopsHostUserPermissionMapper.delete(devopsHostUserPermissionDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseUpdate(Long hostId, List<Long> addUsersList) {
        // 待添加的用户列表
        List<IamUserDTO> addIamUsers = baseServiceClientOperator.listUsersByIds(addUsersList);
        List<DevopsHostUserPermissionDTO> devopsHostUserPermissionDTOList = new ArrayList<>();
        addIamUsers.forEach(e -> devopsHostUserPermissionDTOList.add(new DevopsHostUserPermissionDTO(e.getLoginName(), e.getId(), e.getRealName(), hostId)));
        batchInsert(devopsHostUserPermissionDTOList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseDelete(DevopsHostUserPermissionDTO devopsHostUserPermissionDTO) {
        devopsHostUserPermissionMapper.delete(devopsHostUserPermissionDTO);
    }

    @Override
    public List<DevopsHostUserPermissionDTO> baseListByHostId(Long hostId) {
        return devopsHostUserPermissionMapper.listByHostId(hostId);
    }

    @Override
    public List<Long> listUserIdsByHostId(Long hostId) {
        return devopsHostUserPermissionMapper.listUserIdsByHostId(hostId);
    }
}
