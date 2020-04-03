package io.choerodon.devops.app.service.impl;

import io.choerodon.devops.app.service.DevopsCiContentService;
import io.choerodon.devops.infra.mapper.DevopsCiContentMapper;
import org.springframework.stereotype.Service;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:25
 */
@Service
public class DevopsCiContentServiceImpl implements DevopsCiContentService {
    private DevopsCiContentMapper devopsCiContentMapper;

    public DevopsCiContentServiceImpl(DevopsCiContentMapper devopsCiContentMapper) {
        this.devopsCiContentMapper = devopsCiContentMapper;
    }
}
