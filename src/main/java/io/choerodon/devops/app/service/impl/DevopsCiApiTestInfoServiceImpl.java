package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.pipeline.DevopsCiApiTestInfoVO;
import io.choerodon.devops.app.service.DevopsCiApiTestInfoService;
import io.choerodon.devops.infra.mapper.DevopsCiApiTestInfoMapper;
import io.choerodon.devops.infra.util.ConvertUtils;

@Service
public class DevopsCiApiTestInfoServiceImpl implements DevopsCiApiTestInfoService {
    @Autowired
    private DevopsCiApiTestInfoMapper devopsCiApiTestInfoMapper;

    @Override
    public DevopsCiApiTestInfoVO selectByPrimaryKey(Long id) {
        return ConvertUtils.convertObject(devopsCiApiTestInfoMapper.selectByPrimaryKey(id), DevopsCiApiTestInfoVO.class);
    }
}
