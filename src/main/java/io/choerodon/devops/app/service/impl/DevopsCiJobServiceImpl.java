package io.choerodon.devops.app.service.impl;

import io.choerodon.devops.app.service.DevopsCiJobService;
import io.choerodon.devops.infra.mapper.DevopsCiJobMapper;
import org.springframework.stereotype.Service;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:24
 */
@Service
public class DevopsCiJobServiceImpl implements DevopsCiJobService {
    private DevopsCiJobMapper devopsCiJobMapper;

    public DevopsCiJobServiceImpl(DevopsCiJobMapper devopsCiJobMapper) {
        this.devopsCiJobMapper = devopsCiJobMapper;
    }
}
