package io.choerodon.devops.infra.persistence.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.EnvUserPermissionDTO;
import io.choerodon.devops.domain.application.repository.DevopsEnvUserPermissionRepository;
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

    @Autowired
    private DevopsEnvUserPermissionMapper devopsEnvUserPermissionMapper;

    @Override
    public Page<EnvUserPermissionDTO> pageUserPermission(Long envId, PageRequest pageRequest) {
        return PageHelper.doPage(pageRequest.getPage(), pageRequest.getSize(),
                () -> devopsEnvUserPermissionMapper.pageUserEnvPermission(envId));
    }

    @Override
    public void updateEnvUserPermission(Map<String, Boolean> updateMap, Long envId) {
        Map<String, Boolean> trueMap = new HashMap<>();
        Map<String, Boolean> falseMap = new HashMap<>();
        updateMap.forEach((k, v) -> {
            if (v == true) {
                trueMap.put(k, v);
            } else {
                falseMap.put(k, v);
            }
        });
        devopsEnvUserPermissionMapper.updateEnvUserPermission(trueMap.keySet().stream()
                .collect(Collectors.toList()), true, envId);
        devopsEnvUserPermissionMapper.updateEnvUserPermission(falseMap.keySet().stream()
                .collect(Collectors.toList()), false, envId);
    }
}
