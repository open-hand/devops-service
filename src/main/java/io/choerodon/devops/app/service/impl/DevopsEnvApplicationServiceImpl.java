package io.choerodon.devops.app.service.impl;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.api.dto.ApplicationRepDTO;
import io.choerodon.devops.api.dto.DevopsEnvApplicationDTO;
import io.choerodon.devops.app.service.ApplicationInstanceService;
import io.choerodon.devops.app.service.ApplicationService;
import io.choerodon.devops.app.service.DevopsEnvApplicationService;
import io.choerodon.devops.domain.application.entity.DevopsEnvApplicationE;
import io.choerodon.devops.domain.application.repository.DevopsEnvApplicationRepostitory;
import io.choerodon.devops.infra.dataobject.DevopsEnvApplicationDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lizongwei
 * @date 2019/7/1
 */
@Service
public class DevopsEnvApplicationServiceImpl implements DevopsEnvApplicationService {

    @Autowired
    DevopsEnvApplicationRepostitory devopsEnvApplicationRepostitory;

    @Autowired
    ApplicationService applicationService;

    @Autowired
    ApplicationInstanceService instanceService;

    @Override
    public DevopsEnvApplicationDTO create(DevopsEnvApplicationDTO devopsEnvApplicationDTO) {
        return ConvertHelper.convert(devopsEnvApplicationRepostitory.create(
                ConvertHelper.convert(devopsEnvApplicationDTO,DevopsEnvApplicationE.class)),DevopsEnvApplicationDTO.class);
    }

    @Override
    public List<ApplicationRepDTO> queryAppByEnvId(Long envId) {
        List<Long> appIds =  devopsEnvApplicationRepostitory.queryAppByEnvId(envId);
        return applicationService.queryApps(appIds);
    }

    @Override
    public void syncEnvAppRelevance() {
        List<DevopsEnvApplicationE> envApplicationES = instanceService.listAllEnvApp();
        envApplicationES.stream().distinct().forEach(v->devopsEnvApplicationRepostitory.create(v));
    }
}
