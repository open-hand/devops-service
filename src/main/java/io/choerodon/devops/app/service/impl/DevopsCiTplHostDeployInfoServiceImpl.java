package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.DevopsCiTplHostDeployInfoService;
import io.choerodon.devops.infra.dto.DevopsCiTplHostDeployInfoCfgDTO;
import io.choerodon.devops.infra.mapper.DevopsCiTplHostDeployInfoMapper;

@Service
public class DevopsCiTplHostDeployInfoServiceImpl implements DevopsCiTplHostDeployInfoService {

    @Autowired
    private DevopsCiTplHostDeployInfoMapper devopsCiTplHostDeployInfoMapper;

    @Override
    public DevopsCiTplHostDeployInfoCfgDTO selectByPrimaryKey(Long configId) {
        return devopsCiTplHostDeployInfoMapper.selectByPrimaryKey(configId);
    }
}
