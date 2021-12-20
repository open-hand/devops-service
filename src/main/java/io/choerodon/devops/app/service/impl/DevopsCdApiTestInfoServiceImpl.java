package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.app.service.DevopsCdApiTestInfoService;
import io.choerodon.devops.infra.dto.DevopsCdApiTestInfoDTO;
import io.choerodon.devops.infra.mapper.DevopsCdApiTestInfoMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * devops_cd_api_test_info(DevopsCdApiTestInfo)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-20 09:57:26
 */
@Service
public class DevopsCdApiTestInfoServiceImpl implements DevopsCdApiTestInfoService {
    @Autowired
    private DevopsCdApiTestInfoMapper devopsCdApiTestInfoMapper;


    @Override
    @Transactional
    public void baseCreate(DevopsCdApiTestInfoDTO devopsCdApiTestInfoDTO) {
        MapperUtil.resultJudgedInsertSelective(devopsCdApiTestInfoMapper, devopsCdApiTestInfoDTO, "error.save.api.test.info.failed");
    }

    @Override
    public DevopsCdApiTestInfoDTO queryById(Long deployInfoId) {
        return null;
    }
}

