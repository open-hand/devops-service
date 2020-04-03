package io.choerodon.devops.app.service.impl;

import io.choerodon.devops.app.service.DevopsCiPipelineService;
import io.choerodon.devops.infra.mapper.DevopsCiPipelineMapper;
import org.springframework.stereotype.Service;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/2 18:00
 */
@Service
public class DevopsCiPipelineServiceImpl implements DevopsCiPipelineService{

    private DevopsCiPipelineMapper devopsCiPipelineMapper;

    public DevopsCiPipelineServiceImpl(DevopsCiPipelineMapper devopsCiPipelineMapper) {
        this.devopsCiPipelineMapper = devopsCiPipelineMapper;
    }
}
