package io.choerodon.devops.app.service.impl;

import java.util.List;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.app.service.ApplicationUserPermissionService;
import io.choerodon.devops.infra.dto.ApplicationUserPermissionDTO;
import io.choerodon.devops.infra.feign.operator.IamServiceClientOperator;
import io.choerodon.devops.infra.mapper.ApplicationUserPermissionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Sheep on 2019/7/12.
 */

@Service
public class ApplicationUserPermissionServiceImpl implements ApplicationUserPermissionService {


    @Autowired
    private ApplicationUserPermissionMapper applicationUserPermissionMapper;
    @Autowired
    private IamServiceClientOperator iamServiceClientOperator;



    public void baseCreate(Long userId, Long appId) {
        applicationUserPermissionMapper.insert(new ApplicationUserPermissionDTO(userId, appId));
    }

    public void baseDeleteByAppId(Long appId) {
        ApplicationUserPermissionDTO applicationUserPermissionDTO = new ApplicationUserPermissionDTO();
        applicationUserPermissionDTO.setAppId(appId);
        applicationUserPermissionMapper.delete(applicationUserPermissionDTO);
    }

    public void baseDeleteByUserIdAndAppIds(List<Long> appIds, Long userId) {
        applicationUserPermissionMapper.deleteByUserIdWithAppIds(appIds, userId);
    }

    public List<ApplicationUserPermissionDTO> baseListByAppId(Long appId) {
        return applicationUserPermissionMapper.listAllUserPermissionByAppId(appId);
    }

    public List<ApplicationUserPermissionDTO> baseListByUserId(Long userId) {
        ApplicationUserPermissionDTO applicationUserPermissionDTO = new ApplicationUserPermissionDTO();
        applicationUserPermissionDTO.setIamUserId(userId);
        return applicationUserPermissionMapper.select(applicationUserPermissionDTO);
    }

}
