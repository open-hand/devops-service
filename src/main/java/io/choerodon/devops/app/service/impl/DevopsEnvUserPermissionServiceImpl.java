package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsEnvUserVO;
import io.choerodon.devops.app.service.DevopsEnvUserPermissionService;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.app.service.IamService;
import io.choerodon.devops.infra.dto.DevopsEnvUserPermissionDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.mapper.DevopsEnvUserPermissionMapper;
import io.choerodon.devops.infra.util.TypeUtil;

/**
 * Created by Sheep on 2019/7/11.
 */
@Service
public class DevopsEnvUserPermissionServiceImpl implements DevopsEnvUserPermissionService {


    private static final Gson gson = new Gson();

    @Autowired
    private DevopsEnvUserPermissionMapper devopsEnvUserPermissionMapper;
    @Autowired
    private IamService iamService;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;


    @Override
    public void create(DevopsEnvUserVO devopsEnvUserVO) {
        DevopsEnvUserPermissionDTO devopsEnvUserPermissionDO = new DevopsEnvUserPermissionDTO();
        BeanUtils.copyProperties(devopsEnvUserVO, devopsEnvUserPermissionDO);
        if (devopsEnvUserPermissionMapper.insert(devopsEnvUserPermissionDO) != 1) {
            throw new CommonException("error.devops.env.user.permission.create");
        }
    }

    @Override
    public PageInfo<DevopsEnvUserVO> pageByOptions(Long envId, PageRequest pageRequest,
                                                   String params) {
        Map maps = gson.fromJson(params, Map.class);
        Map<String, Object> searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
        List<String> paramList = TypeUtil.cast(maps.get(TypeUtil.PARAMS));
        PageInfo<DevopsEnvUserVO> devopsEnvUserPermissionDTOPageInfo = PageHelper.startPage(pageRequest.getPage(),
                pageRequest.getSize()).doSelectPageInfo(() -> devopsEnvUserPermissionMapper
                .listUserEnvPermissionByOption(envId, searchParamMap, paramList));

        PageInfo<DevopsEnvUserVO> devopsEnvUserPermissionVOPageInfo = new PageInfo<>();
        BeanUtils.copyProperties(devopsEnvUserPermissionDTOPageInfo, devopsEnvUserPermissionVOPageInfo);
        return devopsEnvUserPermissionVOPageInfo;
    }

    @Override
    public void deleteByEnvId(Long envId) {
        DevopsEnvUserPermissionDTO dto = new DevopsEnvUserPermissionDTO();
        dto.setEnvId(envId);
        devopsEnvUserPermissionMapper.delete(dto);
    }


    @Override
    public List<DevopsEnvUserVO> listByEnvId(Long envId) {

        List<DevopsEnvUserVO> devopsEnvUserVOS = new ArrayList<>();
        BeanUtils.copyProperties(devopsEnvUserPermissionMapper.listByEnvId(envId), devopsEnvUserVOS);
        return devopsEnvUserVOS;
    }


    @Transactional
    public void updateEnvUserPermission(Long envId, List<Long> addUsersList, List<Long> deleteUsersList) {
        // 待添加的用户列表
        List<IamUserDTO> addIamUsers = iamService.listUsersByIds(addUsersList);
        addIamUsers.forEach(e -> devopsEnvUserPermissionMapper
                .insert(new DevopsEnvUserPermissionDTO(e.getLoginName(), e.getId(), e.getRealName(), envId, true)));
        // 待删除的用户列表
        deleteUsersList.forEach(e -> {
            DevopsEnvUserPermissionDTO devopsEnvUserPermissionDO = new DevopsEnvUserPermissionDTO();
            devopsEnvUserPermissionDO.setIamUserId(e);
            devopsEnvUserPermissionDO.setEnvId(envId);
            devopsEnvUserPermissionMapper.delete(devopsEnvUserPermissionDO);
        });
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
        ProjectDTO projectDTO = iamService.queryIamProject(devopsEnvironmentDTO.getProjectId());
        //判断当前用户是否是项目所有者，如果是，直接跳过校验，如果不是，校验环境权限
        if (!iamService.isProjectOwner(userId, projectDTO)) {
            DevopsEnvUserPermissionDTO devopsEnvUserPermissionDO = new DevopsEnvUserPermissionDTO();
            devopsEnvUserPermissionDO.setIamUserId(userId);
            devopsEnvUserPermissionDO.setEnvId(envId);
            devopsEnvUserPermissionDO = devopsEnvUserPermissionMapper.selectOne(devopsEnvUserPermissionDO);
            if (devopsEnvUserPermissionDO != null && !devopsEnvUserPermissionDO.getPermitted()) {
                throw new CommonException("error.env.user.permission.get");
            }
        }
    }


    @Override
    public void baseCreate(DevopsEnvUserPermissionDTO devopsEnvUserPermissionDTO) {
        if (devopsEnvUserPermissionMapper.insert(devopsEnvUserPermissionDTO) != 1) {
            throw new CommonException("error.insert.env.user.permission");
        }
    }

    @Override
    public PageInfo<DevopsEnvUserPermissionDTO> basePageByOptions(Long envId, PageRequest pageRequest,
                                                                  String params) {
        Map maps = gson.fromJson(params, Map.class);
        Map<String, Object> searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
        List<String> paramList = TypeUtil.cast(maps.get(TypeUtil.PARAMS));
        return PageHelper.startPage(pageRequest.getPage(),
                pageRequest.getSize()).doSelectPageInfo(() -> devopsEnvUserPermissionMapper
                .listUserEnvPermissionByOption(envId, searchParamMap, paramList));
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
        List<IamUserDTO> addIamUsers = iamService.listUsersByIds(addUsersList);
        addIamUsers.forEach(e -> devopsEnvUserPermissionMapper
                .insert(new DevopsEnvUserPermissionDTO(e.getLoginName(), e.getId(), e.getRealName(), envId, true)));
        // 待删除的用户列表
        deleteUsersList.forEach(e -> {
            DevopsEnvUserPermissionDTO devopsEnvUserPermissionDO = new DevopsEnvUserPermissionDTO();
            devopsEnvUserPermissionDO.setIamUserId(e);
            devopsEnvUserPermissionDO.setEnvId(envId);
            devopsEnvUserPermissionMapper.delete(devopsEnvUserPermissionDO);
        });
    }

    @Override
    public List<DevopsEnvUserPermissionDTO> baseListByUserId(Long userId) {
        DevopsEnvUserPermissionDTO devopsEnvUserPermissionDO = new DevopsEnvUserPermissionDTO();
        devopsEnvUserPermissionDO.setIamUserId(userId);
        return devopsEnvUserPermissionMapper.select(devopsEnvUserPermissionDO);
    }

    @Override
    public void baseCheckEnvDeployPermission(Long userId, Long envId) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);
        ProjectDTO projectDTO = iamService.queryIamProject(devopsEnvironmentDTO.getProjectId());
        //判断当前用户是否是项目所有者，如果是，直接跳过校验，如果不是，校验环境权限
        if (!iamService.isProjectOwner(userId, projectDTO)) {
            DevopsEnvUserPermissionDTO devopsEnvUserPermissionDO = new DevopsEnvUserPermissionDTO();
            devopsEnvUserPermissionDO.setIamUserId(userId);
            devopsEnvUserPermissionDO.setEnvId(envId);
            devopsEnvUserPermissionDO = devopsEnvUserPermissionMapper.selectOne(devopsEnvUserPermissionDO);
            if (devopsEnvUserPermissionDO != null && !devopsEnvUserPermissionDO.getPermitted()) {
                throw new CommonException("error.env.user.permission.get");
            }
        }
    }

    @Override
    public void baseDelete(Long envId, Long userId) {
        DevopsEnvUserPermissionDTO devopsEnvUserPermissionDTO = new DevopsEnvUserPermissionDTO();
        devopsEnvUserPermissionDTO.setEnvId(envId);
        devopsEnvUserPermissionDTO.setIamUserId(userId);
        devopsEnvUserPermissionMapper.delete(devopsEnvUserPermissionDTO);
    }

}
