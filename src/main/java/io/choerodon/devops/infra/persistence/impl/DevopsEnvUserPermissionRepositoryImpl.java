package io.choerodon.devops.infra.persistence.impl;

import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.DevopsEnvUserPermissionDTO;
import io.choerodon.devops.domain.application.entity.DevopsEnvUserPermissionE;
import io.choerodon.devops.domain.application.repository.DevopsEnvUserPermissionRepository;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.DevopsEnvUserPermissionDO;
import io.choerodon.devops.infra.mapper.DevopsEnvUserPermissionMapper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by n!Ck
 * Date: 2018/10/26
 * Time: 9:37
 * Description:
 */

@Service
public class DevopsEnvUserPermissionRepositoryImpl implements DevopsEnvUserPermissionRepository {

    private static final Gson gson = new Gson();

    @Autowired
    private DevopsEnvUserPermissionMapper devopsEnvUserPermissionMapper;

    @Override
    public void create(DevopsEnvUserPermissionE devopsEnvUserPermissionE) {
        DevopsEnvUserPermissionDO devopsEnvUserPermissionDO = ConvertHelper
                .convert(devopsEnvUserPermissionE, DevopsEnvUserPermissionDO.class);
        devopsEnvUserPermissionMapper.insert(devopsEnvUserPermissionDO);
    }

    @Override
    public Page<DevopsEnvUserPermissionDTO> pageUserPermissionByOption(Long envId, PageRequest pageRequest,
                                                                       String params) {
        Map maps = gson.fromJson(params, Map.class);
        Map<String, Object> searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
        String paramMap = TypeUtil.cast(maps.get(TypeUtil.PARAM));
        Page<DevopsEnvUserPermissionDTO> devopsEnvUserPermissionDTOPage = PageHelper.doPage(pageRequest.getPage(),
                pageRequest.getSize(), () -> devopsEnvUserPermissionMapper
                        .pageUserEnvPermissionByOption(envId, searchParamMap, paramMap));
        return ConvertPageHelper.convertPage(devopsEnvUserPermissionDTOPage, DevopsEnvUserPermissionDTO.class);
    }

    @Override
    public List<DevopsEnvUserPermissionDTO> listALlUserPermission(Long envId) {
        return ConvertHelper.convertList(devopsEnvUserPermissionMapper.listAllUserPermission(envId),
                DevopsEnvUserPermissionDTO.class);
    }

    @Override
    public void updateEnvUserPermission(Long envId, List<Long> userIds) {
        devopsEnvUserPermissionMapper.initUserPermission(envId);
        devopsEnvUserPermissionMapper.updateEnvUserPermission(envId, userIds);
    }
}
