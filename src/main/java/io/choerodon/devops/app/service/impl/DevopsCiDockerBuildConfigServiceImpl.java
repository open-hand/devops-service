package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.DevopsCiDockerBuildConfigService;
import io.choerodon.devops.infra.dto.DevopsCiDockerBuildConfigDTO;
import io.choerodon.devops.infra.mapper.DevopsCiDockerBuildConfigMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/30 16:25
 */
@Service
public class DevopsCiDockerBuildConfigServiceImpl implements DevopsCiDockerBuildConfigService {
    @Autowired
    private DevopsCiDockerBuildConfigMapper devopsCiDockerBuildConfigMapper;

    @Override
    public DevopsCiDockerBuildConfigDTO baseQuery(Long id) {
        return devopsCiDockerBuildConfigMapper.selectByPrimaryKey(id);
    }
}
