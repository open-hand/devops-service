package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.DevopsCiTplHostDeployInfoService;
import io.choerodon.devops.infra.dto.CiTplHostDeployInfoCfgDTO;
import io.choerodon.devops.infra.mapper.CiTplHostDeployInfoMapper;

@Service
public class DevopsCiTplHostDeployInfoServiceImpl implements DevopsCiTplHostDeployInfoService {

    @Autowired
    private CiTplHostDeployInfoMapper ciTplHostDeployInfoMapper;

    @Override
    public CiTplHostDeployInfoCfgDTO selectByPrimaryKey(Long configId) {
        return ciTplHostDeployInfoMapper.selectByPrimaryKey(configId);
    }
}
