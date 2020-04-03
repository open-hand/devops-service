package io.choerodon.devops.app.service.impl;

import io.choerodon.devops.app.service.DevopsCiPipelineRecordService;
import io.choerodon.devops.infra.mapper.DevopsCiPipelineRecordMapper;
import org.springframework.stereotype.Service;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:26
 */
@Service
public class DevopsCiPipelineRecordServiceImpl implements DevopsCiPipelineRecordService {
    private DevopsCiPipelineRecordMapper devopsCiPipelineRecordMapper;

    public DevopsCiPipelineRecordServiceImpl(DevopsCiPipelineRecordMapper devopsCiPipelineRecordMapper) {
        this.devopsCiPipelineRecordMapper = devopsCiPipelineRecordMapper;
    }
}
