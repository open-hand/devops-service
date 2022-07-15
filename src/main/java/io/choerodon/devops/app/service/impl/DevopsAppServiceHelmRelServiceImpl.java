package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.DevopsAppServiceHelmRelService;
import io.choerodon.devops.infra.dto.DevopsAppServiceHelmRelDTO;
import io.choerodon.devops.infra.mapper.DevopsAppServiceHelmRelMapper;
import io.choerodon.devops.infra.util.MapperUtil;

@Service
public class DevopsAppServiceHelmRelServiceImpl implements DevopsAppServiceHelmRelService {
    @Autowired
    private DevopsAppServiceHelmRelMapper devopsAppServiceHelmRelMapper;


    @Override
    public void handleRel(Long appServiceId, Long helmConfigId) {
        deleteRelationByServiceId(appServiceId);
        if (helmConfigId!=null){
            createRel(appServiceId, helmConfigId);
        }
    }

    @Override
    public void deleteRelationByServiceId(Long appServiceId) {
        DevopsAppServiceHelmRelDTO devopsAppServiceHelmRelDTO = new DevopsAppServiceHelmRelDTO();
        devopsAppServiceHelmRelDTO.setAppServiceId(appServiceId);
        devopsAppServiceHelmRelMapper.delete(devopsAppServiceHelmRelDTO);
    }

    @Override
    public void createRel(Long appServiceId, Long helmConfigId) {
        DevopsAppServiceHelmRelDTO devopsAppServiceHelmRelDTO = new DevopsAppServiceHelmRelDTO();
        devopsAppServiceHelmRelDTO.setAppServiceId(appServiceId);
        devopsAppServiceHelmRelDTO.setHelmConfigId(helmConfigId);
        MapperUtil.resultJudgedInsertSelective(devopsAppServiceHelmRelMapper, devopsAppServiceHelmRelDTO, "error.app.service.helm.config.rel.insert");
    }
}
