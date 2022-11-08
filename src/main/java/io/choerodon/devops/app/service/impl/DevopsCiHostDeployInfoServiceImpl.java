package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.DevopsCiHostDeployInfoService;
import io.choerodon.devops.infra.dto.DevopsCiHostDeployInfoDTO;
import io.choerodon.devops.infra.mapper.DevopsCiHostDeployInfoMapper;

@Service
public class DevopsCiHostDeployInfoServiceImpl implements DevopsCiHostDeployInfoService {
    @Autowired
    private DevopsCiHostDeployInfoMapper devopsCiHostDeployInfoMapper;


    @Override
    public void baseUpdate(DevopsCiHostDeployInfoDTO devopsCiHostDeployInfoDTO) {
        devopsCiHostDeployInfoMapper.updateByPrimaryKeySelective(devopsCiHostDeployInfoDTO);
    }
}
