package io.choerodon.devops.app.service.impl;

import io.choerodon.devops.app.service.DevopsCiJobRecordService;
import io.choerodon.devops.infra.mapper.DevopsCiJobRecordMapper;
import org.springframework.stereotype.Service;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:27
 */
@Service
public class DevopsCiJobRecordServiceImpl implements DevopsCiJobRecordService {
    private DevopsCiJobRecordMapper devopsCiJobRecordMapper;

    public DevopsCiJobRecordServiceImpl(DevopsCiJobRecordMapper devopsCiJobRecordMapper) {
        this.devopsCiJobRecordMapper = devopsCiJobRecordMapper;
    }
}
