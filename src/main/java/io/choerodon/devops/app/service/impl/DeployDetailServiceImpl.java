package io.choerodon.devops.app.service.impl;

import java.util.List;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.api.vo.DevopsEnvPodDTO;
import io.choerodon.devops.app.service.DeployDetailService;
import io.choerodon.devops.domain.application.repository.DeployDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Creator: Runge
 * Date: 2018/4/17
 * Time: 14:40
 * Description:
 */
@Component
public class DeployDetailServiceImpl implements DeployDetailService {

    @Autowired
    private DeployDetailRepository deployDetailRepository;

    @Override
    public List<DevopsEnvPodDTO> getPods(Long instanceId) {
        return ConvertHelper.convertList(deployDetailRepository.getPods(instanceId), DevopsEnvPodDTO.class);
    }
}
