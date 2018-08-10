package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.DevopsEnvFileErrorDTO;
import io.choerodon.devops.app.service.DevopsEnvFileService;
import io.choerodon.devops.domain.application.repository.DevopsEnvFileErrorRepository;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Creator: Runge
 * Date: 2018/8/10
 * Time: 11:03
 * Description:
 */
@Service
public class DevopsEnvFileServiceImpl implements DevopsEnvFileService {
    @Autowired
    private DevopsEnvFileErrorRepository devopsEnvFileErrorRepository;

    @Override
    public List<DevopsEnvFileErrorDTO> listByEnvId(Long envId) {
        return ConvertHelper.convertList(
                devopsEnvFileErrorRepository.listByEnvId(envId), DevopsEnvFileErrorDTO.class);
    }

    @Override
    public Page<DevopsEnvFileErrorDTO> pageByEnvId(Long envId, PageRequest pageRequest) {
        return ConvertPageHelper.convertPage(
                devopsEnvFileErrorRepository.pageByEnvId(envId, pageRequest), DevopsEnvFileErrorDTO.class);
    }
}
