package io.choerodon.devops.app.service.impl;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.api.dto.DevopsAppResourceDTO;
import io.choerodon.devops.api.validator.AppResourceValidator;
import io.choerodon.devops.app.service.DevopsAppResourceService;
import io.choerodon.devops.domain.application.entity.DevopsAppResourceE;
import io.choerodon.devops.domain.application.repository.DevopsAppResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lizongwei
 * @date 2019/7/3
 */
@Service
public class DevopsAppResourceServiceImpl implements DevopsAppResourceService {

    @Autowired
    DevopsAppResourceRepository appResourceRepository;

    @Override
    public void insert(DevopsAppResourceDTO devopsAppResourceDTO) {
        appResourceRepository.insert(ConvertHelper.convert(
                devopsAppResourceDTO, DevopsAppResourceE.class));
    }

    @Override
    public void deleteByAppIdAndType(Long appId, String type) {
        appResourceRepository.deleteByAppIdAndType(appId, AppResourceValidator.checkResourceType(type));
    }

    @Override
    public List<DevopsAppResourceDTO> queryByAppAndType(Long appId, String type) {
        return ConvertHelper.convertList(appResourceRepository.queryByAppAndType(appId,
                AppResourceValidator.checkResourceType(type)), DevopsAppResourceDTO.class);
    }

}
