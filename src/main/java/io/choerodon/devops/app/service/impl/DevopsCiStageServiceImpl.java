package io.choerodon.devops.app.service.impl;

import io.choerodon.devops.app.service.DevopsCiStageService;
import io.choerodon.devops.infra.mapper.DevopsCiStageMapper;
import org.springframework.stereotype.Service;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:23
 */
@Service
public class DevopsCiStageServiceImpl implements DevopsCiStageService {
    private DevopsCiStageMapper devopsCiStageMapper;

    public DevopsCiStageServiceImpl(DevopsCiStageMapper devopsCiStageMapper) {
        this.devopsCiStageMapper = devopsCiStageMapper;
    }
}
