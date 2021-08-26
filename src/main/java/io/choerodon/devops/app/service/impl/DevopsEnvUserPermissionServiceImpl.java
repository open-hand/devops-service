package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsUserVO;
import io.choerodon.devops.app.service.DevopsEnvUserPermissionService;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.app.service.PermissionHelper;
import io.choerodon.devops.infra.dto.DevopsEnvUserPermissionDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.UserProjectLabelVO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsEnvUserPermissionMapper;
import io.choerodon.devops.infra.util.ArrayUtil;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by Sheep on 2019/7/11.
 */
@Service
public class DevopsEnvUserPermissionServiceImpl implements DevopsEnvUserPermissionService {
    @Autowired
    private DevopsEnvUserPermissionMapper devopsEnvUserPermissionMapper;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private PermissionHelper permissionHelper;

    @Override
    public Page<DevopsUserVO> pageByOptions(Long envId, PageRequest pageable,
                                            String params) {
        Map<String, Object> maps = TypeUtil.castMapParams(params);
        Map<String, Object> searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
        List<String> paramList = TypeUtil.cast(maps.get(TypeUtil.PARAMS));

        return ConvertUtils.convertPage(
                PageHelper.doPage(pageable, () -> devopsEnvUserPermissionMapper
                        .listUserEnvPermissionByOption(envId, searchParamMap, paramList)),
                DevopsUserVO.class);
    }

    @Override
    public void deleteByEnvId(Long envId) {
        DevopsEnvUserPermissionDTO dto = new DevopsEnvUserPermissionDTO();
        dto.setEnvId(envId);
        devopsEnvUserPermissionMapper.delete(dto);
    }

    @Override
    public List<DevopsUserVO> listByEnvId(Long envId) {
        return ConvertUtils.convertList(devopsEnvUserPermissionMapper.listByEnvId(envId), DevopsUserVO.class);
    }

    @Override
    public List<DevopsEnvUserPermissionDTO> listByUserId(Long userId) {
        DevopsEnvUserPermissionDTO devopsEnvUserPermissionDO = new DevopsEnvUserPermissionDTO();
        devopsEnvUserPermissionDO.setIamUserId(userId);
        return devopsEnvUserPermissionMapper.select(devopsEnvUserPermissionDO);
    }

    @Override
    public void checkEnvDeployPermission(Long userId, Long envId) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);
        checkEnvDeployPermission(userId, devopsEnvironmentDTO);
    }

    @Override
    public void checkEnvDeployPermission(Long userId, DevopsEnvironmentDTO devopsEnvironmentDTO) {
        Long envId = devopsEnvironmentDTO.getId();
        // 判断环境是否跳过权限校验
        if (Boolean.TRUE.equals(devopsEnvironmentDTO.getSkipCheckPermission())) {
            return;
        }
        // 判断当前用户是否是项目所有者或者root，如果是，直接跳过校验，如果不是，校验环境权限
        if (!permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(devopsEnvironmentDTO.getProjectId(), userId)) {
            DevopsEnvUserPermissionDTO devopsEnvUserPermissionDO = new DevopsEnvUserPermissionDTO(envId, userId);
            devopsEnvUserPermissionDO = devopsEnvUserPermissionMapper.selectOne(devopsEnvUserPermissionDO);
            if (devopsEnvUserPermissionDO == null) {
                throw new CommonException("error.env.user.permission.get");
            }
        }
    }

    @Override
    public boolean userFromWebsocketHasPermission(Long userId, DevopsEnvironmentDTO devopsEnvironmentDTO) {
        Long envId = devopsEnvironmentDTO.getId();
        // 判断当前用户是否是项目所有者或者root，如果是，直接跳过校验，如果不是，校验环境权限
        if (!permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(devopsEnvironmentDTO.getProjectId(), userId)) {
            // 判断环境是否跳过权限校验
            if (Boolean.TRUE.equals(devopsEnvironmentDTO.getSkipCheckPermission())) {
                // 如果跳过校验，还要校验用户是否在项目下有角色
                List<UserProjectLabelVO> roleLabels = baseServiceClientOperator.listRoleLabelsForUserInTheProject(userId, ArrayUtil.singleAsSet(devopsEnvironmentDTO.getProjectId()));
                return !CollectionUtils.isEmpty(roleLabels) && !CollectionUtils.isEmpty(roleLabels.get(0).getRoleLabels());
            }
            DevopsEnvUserPermissionDTO devopsEnvUserPermissionDO = new DevopsEnvUserPermissionDTO(envId, userId);
            return devopsEnvUserPermissionMapper.selectCount(devopsEnvUserPermissionDO) > 0;
        }
        return true;
    }

    @Override
    public void baseCreate(DevopsEnvUserPermissionDTO devopsEnvUserPermissionDTO) {
        if (devopsEnvUserPermissionMapper.insert(devopsEnvUserPermissionDTO) != 1) {
            throw new CommonException("error.insert.host.user.permission");
        }
    }

    @Override
    public List<DevopsEnvUserPermissionDTO> baseListByEnvId(Long envId) {
        return devopsEnvUserPermissionMapper.listByEnvId(envId);
    }

    @Override
    public List<DevopsEnvUserPermissionDTO> baseListAll(Long envId) {
        return devopsEnvUserPermissionMapper.listAll(envId);
    }

    @Override
    @Transactional
    public void baseUpdate(Long envId, List<Long> addUsersList, List<Long> deleteUsersList) {
        // 待添加的用户列表
        List<IamUserDTO> addIamUsers = baseServiceClientOperator.listUsersByIds(addUsersList);
        addIamUsers.forEach(e -> devopsEnvUserPermissionMapper
                .insert(new DevopsEnvUserPermissionDTO(e.getLoginName(), e.getId(), e.getRealName(), envId, true)));
        // 待删除的用户列表
        deleteUsersList.forEach(userId -> baseDelete(envId, userId));
    }

    @Override
    public void baseDelete(Long envId, Long userId) {
        DevopsEnvUserPermissionDTO devopsEnvUserPermissionDTO = new DevopsEnvUserPermissionDTO(envId, userId);
        devopsEnvUserPermissionMapper.delete(devopsEnvUserPermissionDTO);
    }

    @Override
    public void batchDelete(List<Long> envIds, Long userId) {
        devopsEnvUserPermissionMapper.batchDelete(envIds, userId);
    }

    @Override
    public Boolean checkUserEnvPermission(Long envId, Long userId) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);
        // 判断环境是否校验权限
        if (Boolean.TRUE.equals(devopsEnvironmentDTO.getSkipCheckPermission())) {
            return true;
        }
        // 判断当前用户是否是项目所有者或者root，如果是，直接跳过校验，如果不是，校验环境权限
        if (!permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(devopsEnvironmentDTO.getProjectId(), userId)) {
            DevopsEnvUserPermissionDTO devopsEnvUserPermissionDO = new DevopsEnvUserPermissionDTO(envId, userId);
            devopsEnvUserPermissionDO = devopsEnvUserPermissionMapper.selectOne(devopsEnvUserPermissionDO);
            return devopsEnvUserPermissionDO != null && devopsEnvUserPermissionDO.getPermitted();
        }
        return true;
    }
}
