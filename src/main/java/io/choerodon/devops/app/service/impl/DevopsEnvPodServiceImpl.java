package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.DevopsEnvPodDTO;
import io.choerodon.devops.app.service.DevopsEnvPodService;
import io.choerodon.devops.domain.application.entity.DevopsEnvPodE;
import io.choerodon.devops.domain.application.repository.DevopsEnvPodRepository;
import io.choerodon.devops.infra.common.util.EnvUtil;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.websocket.helper.EnvListener;

/**
 * Created by Zenger on 2018/4/17.
 */
@Service
public class DevopsEnvPodServiceImpl implements DevopsEnvPodService {


    @Autowired
    private EnvListener envListener;
    @Autowired
    private EnvUtil envUtil;
    @Autowired
    private DevopsEnvPodRepository devopsEnvPodRepository;


    @Override
    public Page<DevopsEnvPodDTO> listAppPod(Long projectId, Long envId, Long appId, PageRequest pageRequest, String searchParam) {
        List<Long> connectedEnvList = envUtil.getConnectedEnvList(envListener);
        List<Long> updatedEnvList = envUtil.getUpdatedEnvList(envListener);
        Page<DevopsEnvPodE> devopsEnvPodEPage = devopsEnvPodRepository.listAppPod(projectId, envId, appId, pageRequest, searchParam);
        devopsEnvPodEPage.stream().forEach(devopsEnvPodE -> {
            if (connectedEnvList.contains(devopsEnvPodE.getEnvId())
                    && updatedEnvList.contains(devopsEnvPodE.getEnvId())) {
                devopsEnvPodE.setConnect(true);
            }
        });

        return ConvertPageHelper.convertPage(
                devopsEnvPodEPage, DevopsEnvPodDTO.class);
    }
}
