package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsEnvUserPermissionDTO;
import io.choerodon.devops.api.vo.DevopsEnvUserPermissionVO;
import io.choerodon.devops.api.vo.ProjectVO;
import io.choerodon.devops.app.service.DevopsEnvUserPermissionService;
import io.choerodon.devops.infra.dto.DevopsEnvUserPermissionDO;
import io.choerodon.devops.infra.feign.operator.IamServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsEnvUserPermissionMapper;
import io.choerodon.devops.infra.util.TypeUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by Sheep on 2019/7/11.
 */
@Service
public class DevopsEnvUserPermissionServiceImpl implements DevopsEnvUserPermissionService {


    private static final Gson gson = new Gson();

    @Autowired
    private DevopsEnvUserPermissionMapper devopsEnvUserPermissionMapper;
    @Autowired
    private IamServiceClientOperator iamServiceClientOperator;

    @Override
    public void create(DevopsEnvUserPermissionVO devopsEnvUserPermissionVO) {
        DevopsEnvUserPermissionDO devopsEnvUserPermissionDO = new DevopsEnvUserPermissionDO();
        BeanUtils.copyProperties(devopsEnvUserPermissionVO, devopsEnvUserPermissionDO);
        if (devopsEnvUserPermissionMapper.insert(devopsEnvUserPermissionDO) != 1) {
            throw new CommonException("error.devops.env.user.permission.create");
        }
    }

    @Override
    public PageInfo<DevopsEnvUserPermissionVO> pageByOptions(Long envId, PageRequest pageRequest,
                                                                          String params) {
        Map maps = gson.fromJson(params, Map.class);
        Map<String, Object> searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
        String paramMap = TypeUtil.cast(maps.get(TypeUtil.PARAM));
        PageInfo<DevopsEnvUserPermissionDTO> devopsEnvUserPermissionDTOPageInfo = PageHelper.startPage(pageRequest.getPage(),
                pageRequest.getSize()).doSelectPageInfo(() -> devopsEnvUserPermissionMapper
                .pageUserEnvPermissionByOption(envId, searchParamMap, paramMap));

        PageInfo<DevopsEnvUserPermissionVO> devopsEnvUserPermissionVOPageInfo = new PageInfo<>();
        BeanUtils.copyProperties(devopsEnvUserPermissionDTOPageInfo,devopsEnvUserPermissionVOPageInfo);
        return devopsEnvUserPermissionVOPageInfo;
    }

    @Override
    public List<DevopsEnvUserPermissionVO> listByEnvId(Long envId) {

        List<DevopsEnvUserPermissionVO> devopsEnvUserPermissionVOS = new ArrayList<>();
        BeanUtils.copyProperties(devopsEnvUserPermissionMapper.listByEnvId(envId),devopsEnvUserPermissionVOS);
        return devopsEnvUserPermissionVOS;
    }


    @Override
    @Transactional
    public void updateEnvUserPermission(Long envId, List<Long> addUsersList, List<Long> deleteUsersList) {
        // 待添加的用户列表
        List<User> addIamUsers = iamRepository.listUsersByIds(addUsersList);
        addIamUsers.forEach(e -> devopsEnvUserPermissionMapper
                .insert(new DevopsEnvUserPermissionDO(e.getLoginName(), e.getId(), e.getRealName(), envId, true)));
        // 待删除的用户列表
        deleteUsersList.forEach(e -> {
            DevopsEnvUserPermissionDO devopsEnvUserPermissionDO = new DevopsEnvUserPermissionDO();
            devopsEnvUserPermissionDO.setIamUserId(e);
            devopsEnvUserPermissionDO.setEnvId(envId);
            devopsEnvUserPermissionMapper.delete(devopsEnvUserPermissionDO);
        });
    }

    @Override
    public List<DevopsEnvUserPermissionE> listByUserId(Long userId) {
        DevopsEnvUserPermissionDO devopsEnvUserPermissionDO = new DevopsEnvUserPermissionDO();
        devopsEnvUserPermissionDO.setIamUserId(userId);
        return ConvertHelper.convertList(devopsEnvUserPermissionMapper.select(devopsEnvUserPermissionDO),
                DevopsEnvUserPermissionE.class);
    }

    @Override
    public void checkEnvDeployPermission(Long userId, Long envId) {
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(envId);
        ProjectVO projectE = iamRepository.queryIamProject(devopsEnvironmentE.getProjectE().getId());
        //判断当前用户是否是项目所有者，如果是，直接跳过校验，如果不是，校验环境权限
        if (!iamRepository.isProjectOwner(userId, projectE)) {
            DevopsEnvUserPermissionDO devopsEnvUserPermissionDO = new DevopsEnvUserPermissionDO();
            devopsEnvUserPermissionDO.setIamUserId(userId);
            devopsEnvUserPermissionDO.setEnvId(envId);
            devopsEnvUserPermissionDO = devopsEnvUserPermissionMapper.selectOne(devopsEnvUserPermissionDO);
            if (devopsEnvUserPermissionDO != null && !devopsEnvUserPermissionDO.getPermitted()) {
                throw new CommonException("error.env.user.permission.get");
            }
        }
    }

}
