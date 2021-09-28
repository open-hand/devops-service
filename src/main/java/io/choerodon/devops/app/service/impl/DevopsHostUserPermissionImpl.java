package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hzero.mybatis.BatchInsertHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsHostService;
import io.choerodon.devops.app.service.DevopsHostUserPermissionService;
import io.choerodon.devops.app.service.PermissionHelper;
import io.choerodon.devops.infra.dto.DevopsHostDTO;
import io.choerodon.devops.infra.dto.DevopsHostUserPermissionDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsHostUserPermissionMapper;

@Service
public class DevopsHostUserPermissionImpl implements DevopsHostUserPermissionService {

    private static final String ERROR_NOT_OWNED_HOST_PERMISSION = "error.not.owned.host.permission";

    @Autowired
    private DevopsHostUserPermissionMapper devopsHostUserPermissionMapper;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    @Lazy
    private DevopsHostService devopsHostService;
    @Autowired
    private PermissionHelper permissionHelper;

    @Autowired
    @Qualifier("devopsHostUserPermissionInsertHelper")
    private BatchInsertHelper<DevopsHostUserPermissionDTO> batchInsertHelper;

    @Override
    public void checkUserPermissionAndThrow(Long projectId, Long hostId, Long userId) {
        // 1. 主机是跳过权限校验的直接放行
        DevopsHostDTO devopsHostDTO = devopsHostService.baseQuery(hostId);
        if (Boolean.TRUE.equals(devopsHostDTO.getSkipCheckPermission())) {
            return;
        }
        // 2. 用户是root用户、项目所有者、主机创建者也直接放行
        if (permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(projectId, userId)
                || devopsHostDTO.getCreatedBy().equals(userId)) {
            return;
        }
        // 3. 判断用户是否在devops_host_user_permission表中
        DevopsHostUserPermissionDTO devopsHostUserPermissionDTO = new DevopsHostUserPermissionDTO();
        devopsHostUserPermissionDTO.setHostId(hostId);
        devopsHostUserPermissionDTO.setIamUserId(userId);
        if (CollectionUtils.isEmpty(devopsHostUserPermissionMapper.select(devopsHostUserPermissionDTO))) {
            throw new CommonException(ERROR_NOT_OWNED_HOST_PERMISSION);
        }

    }

    @Override
    public Boolean checkUserPermission(Long projectId, Long hostId, Long userId) {
        // 1. 主机是跳过权限校验的直接放行
        DevopsHostDTO devopsHostDTO = devopsHostService.baseQuery(hostId);
        if (Boolean.TRUE.equals(devopsHostDTO.getSkipCheckPermission())) {
            return true;
        }
        // 2. 用户是root用户、项目所有者、主机创建者也直接放行
        if (permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(projectId, userId)
                || devopsHostDTO.getCreatedBy().equals(userId)) {
            return true;
        }
        // 3. 判断用户是否在devops_host_user_permission表中
        DevopsHostUserPermissionDTO devopsHostUserPermissionDTO = new DevopsHostUserPermissionDTO();
        devopsHostUserPermissionDTO.setHostId(hostId);
        devopsHostUserPermissionDTO.setIamUserId(userId);
        if (!CollectionUtils.isEmpty(devopsHostUserPermissionMapper.select(devopsHostUserPermissionDTO))) {
            return true;
        }
        return false;
    }

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

    @Override
    public List<DevopsHostUserPermissionDTO> listUserHostPermissionByOption(Long hostId, Map<String, Object> searchParamMap, List<String> paramList) {
        return devopsHostUserPermissionMapper.listUserHostPermissionByOption(hostId, searchParamMap, paramList);
    }
}
