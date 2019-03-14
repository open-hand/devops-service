package io.choerodon.devops.app.service.impl;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.DevopsProjectConfigDTO;
import io.choerodon.devops.api.validator.DevopsProjectConfigValidator;
import io.choerodon.devops.app.service.DevopsProjectConfigService;
import io.choerodon.devops.domain.application.entity.DevopsProjectConfigE;
import io.choerodon.devops.domain.application.repository.DevopsProjectConfigRepository;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/11
 */
@Service
public class DevopsProjectConfigServiceImpl implements DevopsProjectConfigService {

    @Autowired
    DevopsProjectConfigRepository devopsProjectConfigRepository;

    @Autowired
    DevopsProjectConfigValidator configValidator;

    @Override
    public DevopsProjectConfigDTO create(Long projectId, DevopsProjectConfigDTO devopsProjectConfigDTO) {
        DevopsProjectConfigE devopsProjectConfigE = ConvertHelper.convert(devopsProjectConfigDTO, DevopsProjectConfigE.class);
        devopsProjectConfigE.setProjectId(projectId);
        configValidator.checkConfigType(devopsProjectConfigDTO);
        return ConvertHelper.convert(devopsProjectConfigRepository.create(devopsProjectConfigE), DevopsProjectConfigDTO.class);
    }

    @Override
    public DevopsProjectConfigDTO updateByPrimaryKeySelective(Long projectId, DevopsProjectConfigDTO devopsProjectConfigDTO) {
        DevopsProjectConfigE devopsProjectConfigE = ConvertHelper.convert(devopsProjectConfigDTO, DevopsProjectConfigE.class);
        if(!ObjectUtils.isEmpty(devopsProjectConfigDTO.getType())){
            configValidator.checkConfigType(devopsProjectConfigDTO);
        }
        return ConvertHelper.convert(devopsProjectConfigRepository.updateByPrimaryKeySelective(devopsProjectConfigE), DevopsProjectConfigDTO.class);
    }

    @Override
    public DevopsProjectConfigDTO queryByPrimaryKey(Long id) {
        return ConvertHelper.convert(devopsProjectConfigRepository.queryByPrimaryKey(id),DevopsProjectConfigDTO.class);
    }

    @Override
    public Page<DevopsProjectConfigDTO> listByOptions(Long projectId, PageRequest pageRequest, String params) {
        return ConvertPageHelper.convertPage(devopsProjectConfigRepository.listByOptions(projectId,pageRequest,params),DevopsProjectConfigDTO.class);
    }

    @Override
    public void delete(Long id) {
        devopsProjectConfigRepository.delete(id);
    }

    @Override
    public List<DevopsProjectConfigDTO> queryByIdAndType(Long projectId, String type) {
        return ConvertHelper.convertList(devopsProjectConfigRepository.queryByIdAndType(projectId, type), DevopsProjectConfigDTO.class);
    }
}
