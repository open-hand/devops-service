package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hzero.mybatis.BatchInsertHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsHostService;
import io.choerodon.devops.app.service.DevopsHostUserPermissionService;
import io.choerodon.devops.app.service.PermissionHelper;
import io.choerodon.devops.infra.dto.DevopsHostDTO;
import io.choerodon.devops.infra.dto.DevopsHostUserPermissionDTO;
import io.choerodon.devops.infra.enums.DevopsHostUserPermissionLabelEnums;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsHostUserPermissionMapper;

@Service
public class DevopsHostUserPermissionImpl implements DevopsHostUserPermissionService {

    @Autowired
    private DevopsHostUserPermissionMapper devopsHostUserPermissionMapper;
    @Autowired
    private DevopsHostService devopsHostService;
    @Autowired
    private PermissionHelper permissionHelper;
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
    public void baseDelete(DevopsHostUserPermissionDTO devopsHostUserPermissionDTO) {
        devopsHostUserPermissionMapper.delete(devopsHostUserPermissionDTO);
    }

    @Override
    public List<Long> listUserIdsByHostId(Long hostId) {
        return devopsHostUserPermissionMapper.listUserIdsByHostId(hostId);
    }

    @Override
    public List<DevopsHostUserPermissionDTO> listUserHostPermissionByOption(Long hostId, Map<String, Object> searchParamMap, List<String> paramList) {
        return devopsHostUserPermissionMapper.listUserHostPermissionByOption(hostId, searchParamMap, paramList);
    }

    @Override
    public void checkUserOwnUsePermissionOrThrow(Long projectId, DevopsHostDTO devopsHostDTO, Long userId) {
        // 检查用户为主机创建者
        if (devopsHostDTO.getCreatedBy().equals(userId)) {
            return;
        }
        // 检查用户为项目所有者
        boolean projectOwnerOrRoot = permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(projectId);
        if (projectOwnerOrRoot) {
            return;
        }
        // 检查用户有权限（只要有权限标签，就代表有使用权限）
        if (!StringUtils.isEmpty(devopsHostUserPermissionMapper.queryPermissionLabelByHostIdAndUserId(devopsHostDTO.getId(), userId))) {
            return;
        }
        // 抛出异常
        throw new CommonException("error.host.user.permission");
    }

    @Override
    public void checkUserOwnManagePermissionOrThrow(Long projectId, DevopsHostDTO devopsHostDTO, Long userId) {
        // 检查用户为主机创建者
        if (devopsHostDTO.getCreatedBy().equals(userId)) {
            return;
        }
        // 检查用户为项目所有者
        boolean projectOwnerOrRoot = permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(projectId);
        if (projectOwnerOrRoot) {
            return;
        }
        // 检查用户有主机管理权限（必须拥有administrator标签，才有管理权限）
        String permissionLabel = devopsHostUserPermissionMapper.queryPermissionLabelByHostIdAndUserId(devopsHostDTO.getId(), userId);
        if (DevopsHostUserPermissionLabelEnums.ADMINISTRATOR.getValue().equals(permissionLabel)) {
            return;
        }
        // 抛出异常
        throw new CommonException("error.host.user.permission");
    }

    @Override
    public List<DevopsHostUserPermissionDTO> listUserHostPermissionByUserIdAndHostIds(Long userId, List<Long> hostIds) {
        if (CollectionUtils.isEmpty(hostIds)) {
            return new ArrayList<>();
        }
        return devopsHostUserPermissionMapper.listUserHostPermissionByUserIdAndHostIds(userId, hostIds);
    }

    @Override
    public Boolean checkUserOwnUsePermission(Long projectId, Long hostId, Long userId) {
        try {
            DevopsHostDTO devopsHostDTO = devopsHostService.baseQuery(hostId);
            checkUserOwnUsePermissionOrThrow(projectId, devopsHostDTO, userId);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
