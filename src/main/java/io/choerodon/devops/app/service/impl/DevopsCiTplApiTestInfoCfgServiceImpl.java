package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.DevopsCiTplApiTestInfoCfgService;
import io.choerodon.devops.infra.dto.CiTplApiTestInfoCfgDTO;
import io.choerodon.devops.infra.mapper.CiTplApiTestInfoCfgMapper;

@Service
public class DevopsCiTplApiTestInfoCfgServiceImpl implements DevopsCiTplApiTestInfoCfgService {
    @Autowired
    private CiTplApiTestInfoCfgMapper ciTplApiTestInfoCfgMapper;

    @Override
    public CiTplApiTestInfoCfgDTO selectByPrimaryKey(Long configId) {
        return ciTplApiTestInfoCfgMapper.selectByPrimaryKey(configId);
    }
}
