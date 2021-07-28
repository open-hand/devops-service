package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.app.service.DevopsHzeroDeployConfigService;
import io.choerodon.devops.infra.dto.deploy.DevopsHzeroDeployConfigDTO;
import io.choerodon.devops.infra.mapper.DevopsHzeroDeployConfigMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/7/28 10:02
 */
@Service
public class DevopsHzeroDeployConfigServiceImpl implements DevopsHzeroDeployConfigService {

    private static final String ERROR_SAVE_DEPLOY_VALUE_FAILED = "error.save.deploy.value.failed";

    @Autowired
    private DevopsHzeroDeployConfigMapper devopsHzeroDeployConfigMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DevopsHzeroDeployConfigDTO baseSave(DevopsHzeroDeployConfigDTO devopsHzeroDeployConfigDTO) {
        MapperUtil.resultJudgedInsert(devopsHzeroDeployConfigMapper, devopsHzeroDeployConfigDTO, ERROR_SAVE_DEPLOY_VALUE_FAILED);
        return devopsHzeroDeployConfigMapper.selectByPrimaryKey(devopsHzeroDeployConfigDTO.getId());
    }

    @Override
    public DevopsHzeroDeployConfigDTO baseQueryById(Long valueId) {
        return devopsHzeroDeployConfigMapper.selectByPrimaryKey(valueId);
    }
}
